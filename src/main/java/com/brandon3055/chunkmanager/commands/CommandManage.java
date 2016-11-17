package com.brandon3055.chunkmanager.commands;

import com.brandon3055.chunkmanager.ChunkLoadingHandler;
import com.brandon3055.chunkmanager.DataManager;
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
import net.minecraftforge.common.ForgeChunkManager;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class CommandManage extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandName() {
        return "chunkmanager";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/chunkmanager";
    }

    @Override
    public List<String> getCommandAliases() {
        List<String> list = new LinkedList<>();
        list.add("cm");
        return list;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            help(sender);
            return;
        }

        //region reload
        if (args[0].equals("reload")) {
            try {
                DataManager.loadConfig();
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new CommandException(e.getMessage());
            }
            sender.addChatMessage(new TextComponentString("Reloaded from disk!"));
            ChunkLoadingHandler.reloadChunks(server);
            sender.addChatMessage(new TextComponentString("Reloaded active chunk tickets!"));
        }
        //endregion

        //region add/subtract
        else if (args[0].equals("add") || args[0].equals("subtract")) {
            EntityPlayer player;
            int number = 1;
            if (args.length > 1) {
                player = getPlayer(server, sender, args[1]);
            }
            else {
                player = getCommandSenderAsPlayer(sender);
            }
            if (args.length > 2) {
                number = parseInt(args[2]);
            }
            if (args[0].equals("subtract")) {
                number = -number;
            }

            DataManager.getUserData(player.getName()).extraChunks += number;
            if (DataManager.getUserData(player.getName()).extraChunks < 0) {
                DataManager.getUserData(player.getName()).extraChunks = 0;
            }

            if (number > 0) {
                player.addChatComponentMessage(new TextComponentString("Added " + number + " chunk(s) to your chunk loading limit!").setStyle(new Style().setColor(TextFormatting.GREEN)));
            }
            else if (number < 0) {
                player.addChatComponentMessage(new TextComponentString("Subtracted " + number + " chunk(s) from your chunk loading limit!").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
            }

            try {
                DataManager.saveConfig();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new CommandException(e.getMessage());
            }
        }
        //endregion

        //region set log cooldown
        else if (args[0].equals("set_logout_cooldown") && args.length == 2) {
            String value = args[1];
            int number = 0;
            if (value.endsWith("s")) {
                number = (int)(parseDouble(value.replace("s", "")) * 20D);
            }
            else if (value.endsWith("m")) {
                number = (int)(parseDouble(value.replace("m", "")) * 1200D);
            }
            else if (value.endsWith("h")) {
                number = (int)(parseDouble(value.replace("h", "")) * 72000D);
            }
            else {
                number = parseInt(value);
            }

            DataManager.logoutCoolDown = number;
            try {
                DataManager.saveConfig();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new CommandException(e.getMessage());
            }

            sender.addChatMessage(new TextComponentString("Logout cooldown set to " + DataManager.getFormattedCooldown() + "(hh:mm:ss)").setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
        //endregion

        //region info
        else if (args[0].equals("info")) {
            sender.addChatMessage(new TextComponentString("======= Chunk Manager Info =======").setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
            twoColourChat(sender, "Logout Cooldown: ", TextFormatting.GOLD, DataManager.getFormattedCooldown() + " (hh:mm:s)", TextFormatting.GREEN);
            twoColourChat(sender, "Active Users: ", TextFormatting.GOLD, ChunkLoadingHandler.playerTickets.size()+"", TextFormatting.GREEN);
            twoColourChat(sender, "Total Users: ", TextFormatting.GOLD, DataManager.userDataList.size()+"", TextFormatting.GREEN);

            int aChunks = 0;
            for (ForgeChunkManager.Ticket ticket : ChunkLoadingHandler.ticketList) {
                aChunks += ticket.getChunkList().size();
            }

            twoColourChat(sender, "Active User Chunks: ", TextFormatting.GOLD, aChunks + " (may contain Duplicates)", TextFormatting.GREEN);

            int allChunks = 0;
            for (UserData userData : DataManager.userDataList) {
                allChunks += userData.chunksLoaded.size();
            }

            twoColourChat(sender, "All User Chunks: ", TextFormatting.GOLD, allChunks + " (may contain Duplicates)", TextFormatting.GREEN);
            sender.addChatMessage(new TextComponentString("ChunkManager. Created by Brandon3055").setStyle(new Style().setColor(TextFormatting.BLUE).setItalic(true)));
        }
        //endregion

        //region set chunk allowanceoad
        else if (args[0].equals("set_chunk_allowance") && args.length == 2) {
            DataManager.baseChunkAllocation = parseInt(args[1]);
            try {
                DataManager.saveConfig();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new CommandException(e.getMessage());
            }
            sender.addChatMessage(new TextComponentString("Base chunk allowance set to " + DataManager.baseChunkAllocation).setStyle(new Style().setColor(TextFormatting.GREEN)));

        }
        //endregion

        //region user_info
        else if (args[0].equals("user_info") && args.length == 2) {
            EntityPlayer player = getPlayer(server, sender, args[1]);
            UserData data = DataManager.getUserData(player.getName());
            String user = player.getName();

            sender.addChatMessage(new TextComponentString("========= Chunk Manager Status =========").setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
            twoColourChat(sender, "Chunk Allowance: ", TextFormatting.GOLD, String.valueOf(data.extraChunks + DataManager.getBaseChunkAllocation(user)), TextFormatting.GREEN);
            twoColourChat(sender, "Loaded Chunks: ", TextFormatting.GOLD, String.valueOf(data.chunksLoaded.size()), TextFormatting.GREEN);
            sender.addChatMessage(new TextComponentString("Chunks:").setStyle(new Style().setColor(TextFormatting.GOLD)));
            for (DataManager.LoadedChunk chunk : data.chunksLoaded) {
                sender.addChatMessage(new TextComponentString(" - [ChunkX: " + ((chunk.chunkX * 16) + 8) + ", ChunkZ: " + ((chunk.chunkZ * 16) + 8) + ", Dimension: " + chunk.dimension +"]").setStyle(new Style().setColor(TextFormatting.GRAY)));
            }
        }
        //endregion

        //region show_user_chunks
        else if (args[0].equals("show_user_chunks")) {
            if (args.length == 1 || ModEventHandler.opChunkDisplay.containsKey(sender.getName())) {
                if (ModEventHandler.opChunkDisplay.containsKey(sender.getName())) {
                    sender.addChatMessage(new TextComponentString("Nolonger showing user chunks for " + ModEventHandler.opChunkDisplay.get(sender.getName())).setStyle(new Style().setColor(TextFormatting.GREEN)));
                    ModEventHandler.opChunkDisplay.remove(sender.getName());
                }
                else {
                    sender.addChatMessage(new TextComponentString("You are not currently viewing any user's chunks."));
                    sender.addChatMessage(new TextComponentString("To visually see a users chunks use /chunkmanager show_user_chunks <player>"));
                }
                return;
            }

            String user = getPlayer(server, sender, args[1]).getName();
            ModEventHandler.opChunkDisplay.put(sender.getName(), user);
            sender.addChatMessage(new TextComponentString("You can now see " + user + "'s loaded chunks (if you are close enough to them)").setStyle(new Style().setColor(TextFormatting.GREEN)));
        }
        //endregion

        else {
            help(sender);
        }
    }

    private void help(ICommandSender sender) {
        sender.addChatMessage(new TextComponentString("Usage:"));
        sender.addChatMessage(new TextComponentString("/chunkmanager info"));
        sender.addChatMessage(new TextComponentString("/chunkmanager reload"));
        sender.addChatMessage(new TextComponentString("/chunkmanager add <player> <number>"));
        sender.addChatMessage(new TextComponentString("/chunkmanager subtract <player> <number>"));
        sender.addChatMessage(new TextComponentString("/chunkmanager set_logout_cooldown [<ticks>, <seconds>s, <minutes>m or <hours>h]"));
        sender.addChatMessage(new TextComponentString("/chunkmanager set_chunk_allowance [number]"));
        sender.addChatMessage(new TextComponentString("/chunkmanager user_info [player]"));
        sender.addChatMessage(new TextComponentString("/chunkmanager show_user_chunks [player]"));
    }

    public static void twoColourChat(ICommandSender sender, String s1, TextFormatting colour1, String s2, TextFormatting colour2) {
        sender.addChatMessage(new TextComponentString(s1).setStyle(new Style().setColor(colour1)).appendSibling(new TextComponentString(s2).setStyle(new Style().setColor(colour2))));
    }

    @Override
    public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "info", "reload", "add", "subtract", "set_logout_cooldown", "set_chunk_allowance", "user_info", "show_user_chunks");//, "load", "unload", "show");
        }
        else if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getAllUsernames());
        }
        return Collections.<String>emptyList();
    }
}
