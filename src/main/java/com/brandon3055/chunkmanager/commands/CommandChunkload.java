package com.brandon3055.chunkmanager.commands;

import com.brandon3055.chunkmanager.ChunkLoadingHandler;
import com.brandon3055.chunkmanager.DataManager;
import com.brandon3055.chunkmanager.DataManager.LoadedChunk;
import com.brandon3055.chunkmanager.DataManager.UserData;
import com.brandon3055.chunkmanager.ModEventHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class CommandChunkload extends CommandBase {

    @Override
    public String getCommandName() {
        return "chunkload";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/chunkload";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            help(sender);
            return;
        }

        UserData data = DataManager.getUserData(sender.getName());
        String user = sender.getName();

        if (args[0].equals("status")) {
            sender.addChatMessage(new TextComponentString("========= Chunk Manager Status =========").setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
            twoColourChat(sender, "Chunk Allowance: ", TextFormatting.GOLD, String.valueOf(data.extraChunks + DataManager.getBaseChunkAllocation(user)), TextFormatting.GREEN);
            twoColourChat(sender, "Loaded Chunks: ", TextFormatting.GOLD, String.valueOf(data.chunksLoaded.size()), TextFormatting.GREEN);
            sender.addChatMessage(new TextComponentString("Chunks:").setStyle(new Style().setColor(TextFormatting.GOLD)));
            for (LoadedChunk chunk : data.chunksLoaded) {
                sender.addChatMessage(new TextComponentString(" - [ChunkX: " + ((chunk.chunkX * 16) + 8) + ", ChunkZ: " + ((chunk.chunkZ * 16) + 8) + ", Dimension: " + chunk.dimension +"]").setStyle(new Style().setColor(TextFormatting.GRAY)));
            }
        }
        else if (args[0].equals("load")) {
            startLoading(getCommandSenderAsPlayer(sender), data, server);
        }
        else if (args[0].equals("unload")) {
            stopLoading(getCommandSenderAsPlayer(sender), data, server);
        }
        else if (args[0].equals("unloadall")) {
            data.chunksLoaded.clear();
            ChunkLoadingHandler.updateLoading(user, server);
        }
        else if (args[0].equals("show")) {
            if (ModEventHandler.chunkDisplay.contains(user)) {
                ModEventHandler.chunkDisplay.remove(user);
                sender.addChatMessage(new TextComponentString("Loaded Chunk Display Deactivated").setStyle(new Style().setColor(TextFormatting.GREEN)));
            }
            else {
                ModEventHandler.chunkDisplay.add(user);
                sender.addChatMessage(new TextComponentString("Loaded Chunk Display Activated (Look for the particles)").setStyle(new Style().setColor(TextFormatting.GREEN)));
            }
        }
        else {
            help(sender);
        }
    }

    private void startLoading(EntityPlayer player, UserData data, MinecraftServer server) throws CommandException {
        LoadedChunk chunk = LoadedChunk.fromPlayer(player);
        String name = player.getName();

        if (data.chunksLoaded.contains(chunk)) {
            player.addChatComponentMessage(new TextComponentString("You are already loading this chunk!").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        if (data.chunksLoaded.size() >= DataManager.getUserChunkAllocation(name)) {
            player.addChatComponentMessage(new TextComponentString("You have reached your chunk allocation limit!").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
            return;
        }

        data.chunksLoaded.add(chunk);
        try {
            DataManager.saveConfig();
        }
        catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
        ChunkLoadingHandler.updateLoading(name, server);
        player.addChatComponentMessage(new TextComponentString("You are now loading this chunk!").setStyle(new Style().setColor(TextFormatting.GREEN)));
    }

    private void stopLoading(EntityPlayer player, UserData data, MinecraftServer server) throws CommandException {
        LoadedChunk chunk = LoadedChunk.fromPlayer(player);

        if (!data.chunksLoaded.contains(chunk)) {
            player.addChatComponentMessage(new TextComponentString("You are not loading this chunk!").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }

        data.chunksLoaded.remove(chunk);
        try {
            DataManager.saveConfig();
        }
        catch (Exception e) {
            throw new CommandException(e.getMessage());
        }
        ChunkLoadingHandler.updateLoading(player.getName(), server);
        player.addChatComponentMessage(new TextComponentString("You are nolonger loading this chunk!").setStyle(new Style().setColor(TextFormatting.GREEN)));
    }

    private void help(ICommandSender sender) {
        sender.addChatMessage(new TextComponentString("Usage:"));
        sender.addChatMessage(new TextComponentString("/chunkload status"));
        sender.addChatMessage(new TextComponentString("/chunkload load"));
        sender.addChatMessage(new TextComponentString("/chunkload unload"));
        sender.addChatMessage(new TextComponentString("/chunkload unloadall"));
        sender.addChatMessage(new TextComponentString("/chunkload show"));
        sender.addChatMessage(new TextComponentString("Tip: Press F3+G to show chunk boundaries"));
    }

    public static void twoColourChat(ICommandSender sender, String s1, TextFormatting colour1, String s2, TextFormatting colour2) {
        sender.addChatMessage(new TextComponentString(s1).setStyle(new Style().setColor(colour1)).appendSibling(new TextComponentString(s2).setStyle(new Style().setColor(colour2))));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        return getListOfStringsMatchingLastWord(args, "status", "load", "unload", "unloadall", "show");
    }
}
