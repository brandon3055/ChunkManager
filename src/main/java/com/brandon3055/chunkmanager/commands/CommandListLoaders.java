package com.brandon3055.chunkmanager.commands;

import com.brandon3055.chunkmanager.lib.LogHelper;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import java.util.*;

import static net.minecraft.util.text.TextFormatting.*;

/**
 * Created by brandon3055 on 28/11/2015.
 */
public class CommandListLoaders extends CommandBase {

    private static Map<Integer, LinkedList<Ticket>> ticketCache = new HashMap<>();
    private static int cacheHash = 0;


	@Override
	public String getCommandName() {
		return "listchunkloaders";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {
		return "/listchunkloaders";
	}

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0 || (args.length == 2 && args[0].equals("[Ticket-Page]"))) {
            updateCache(server);
            listTickets(args.length == 2 ? parseInt(args[1]) : 0, server, sender, cacheHash);
            return;
        }
        else if (args.length == 5 && args[0].equals("[Show-Chunks]")) {
            if (cacheHash != parseInt(args[4])) {
                sender.addChatMessage(new TextComponentString("Ticket cache has expired! Please run the list command again to refresh the cache.").setStyle(new Style().setColor(TextFormatting.RED)));
                return;
            }

            ticketInfo(parseInt(args[3]), sender, parseInt(args[1]), parseInt(args[2]), cacheHash);
            return;
        }


    }

    @Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	private void updateCache(MinecraftServer server) {
        for (WorldServer worldServer : server.worldServers) {
            LinkedList<Ticket> tickets = new LinkedList<>();

            for (Map.Entry<ChunkPos, Ticket> entry : ForgeChunkManager.getPersistentChunksFor(worldServer).entries()) {
                if (!tickets.contains(entry.getValue())) {
                    tickets.add(entry.getValue());
                }
            }

            if (tickets.size() == 0) {
                continue;
            }

            ticketCache.put(worldServer.provider.getDimension(), tickets);
        }

        cacheHash = ticketCache.hashCode();
    }

    private void listTickets(int page, MinecraftServer server, ICommandSender sender, int hash) {
        sender.addChatMessage(new TextComponentString("================ Ticket List ================").setStyle(new Style().setColor(TextFormatting.AQUA)));

        LinkedList<ITextComponent> components = new LinkedList<>();

        for (int dimension : ticketCache.keySet()) {
            WorldServer dim = server.worldServerForDimension(dimension);
            components.add(new TextComponentString("Tickets for dimension: " + (dim == null ? dimension+"" : dim.provider.getDimensionType().getName())).setStyle(new Style().setColor(TextFormatting.GREEN).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Tickets are used by mods to load chunks via forge. Depending on how the mod implements them each player may have their own tickets. A ticket can load multiple chunks within a dimension but each dimension required a different ticket.")))));
            components.add(new TextComponentString("Index:[Mod : Player : Chunk Count : Data]").setStyle(new Style().setColor(DARK_PURPLE)));

            for (Ticket ticket : ticketCache.get(dimension)) {
                String s = String.format(" %s%s:%s[%s%s : %s : %s : %s%s]", YELLOW, ticketCache.get(dimension).indexOf(ticket), DARK_AQUA, GOLD, ticket.getModId(), ticket.isPlayerTicket() ? ticket.getPlayerName() : "unknown", ticket.getChunkList().size(), ticket.getModData(), DARK_AQUA);

                components.add(new TextComponentString(s).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to show chunk list!")))
                        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " [Show-Chunks] " + dimension + " " + ticketCache.get(dimension).indexOf(ticket) + " " + 0 + " " + hash))));
            }

            components.add(new TextComponentString(""));
        }

        int rows = 18;//page > 0 ? 16 : 8;
        int pages = components.size() / rows;

        for (int i = 0; i < rows; i++) {
            int index = (page * rows) + i;
            if (index >= 0 && index < components.size()) {
                sender.addChatMessage(components.get(index));
            }
            else {
                sender.addChatMessage(new TextComponentString(""));
            }
        }

        ITextComponent start = new TextComponentString("=========").setStyle(new Style().setColor(TextFormatting.AQUA));
        ITextComponent prev = new TextComponentString(" <-Prev ").setStyle(new Style().setColor(TextFormatting.GOLD));

        LogHelper.error(page + " / " + pages);
        if (page > 0) {
            prev.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " [Ticket-Page] " + (page - 1)));
        }

        ITextComponent pageDisplay = new TextComponentString(String.format("[Page: %s/%s]", page + 1, pages + 1));
        ITextComponent next = new TextComponentString(" Next-> ").setStyle(new Style().setColor(TextFormatting.GOLD));

        if (page < pages) {
            next.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " [Ticket-Page] " + (page + 1)));
        }

        ITextComponent end = new TextComponentString("=========").setStyle(new Style().setColor(TextFormatting.AQUA));

        sender.addChatMessage(start.appendSibling(prev).appendSibling(pageDisplay).appendSibling(next).appendSibling(end));
    }

    private void ticketInfo(int page, ICommandSender sender, int dimension, int ticketIndex, int hash) {
        sender.addChatMessage(new TextComponentString("================ Ticket Chunks ================").setStyle(new Style().setColor(TextFormatting.AQUA)));

        Ticket ticket = ticketCache.get(dimension).get(ticketIndex);

        LinkedList<ITextComponent> components = new LinkedList<>();

        List<ChunkPos> list = Lists.newArrayList(ticket.getChunkList());
        for (ChunkPos pos : list) {
            int x = (pos.chunkXPos * 16) + 8;
            int z = (pos.chunkZPos * 16) + 8;
            int y = sender.getEntityWorld().getTopSolidOrLiquidBlock(new BlockPos(x, 255, z)).getY();

            ITextComponent hover = new TextComponentString(sender.getEntityWorld().provider.getDimension() == dimension ? "Click To Teleport To Chunk" : "Can not teleport to chunk because chunk is in a different dimension!");
            ClickEvent click = sender.getEntityWorld().provider.getDimension() == dimension ? new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + x + " " + y + " " + z) : null;

            components.add(new TextComponentString(" " + String.format(list.indexOf(pos) + ":[ChunkX: %s, ChunkZ: %s]", x, z)).setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover)).setClickEvent(click)));
        }

        int rows = 18;//page > 0 ? 16 : 8;
        int pages = components.size() / rows;

        for (int i = 0; i < rows; i++) {
            int index = (page * rows) + i;
            if (index >= 0 && index < components.size()) {
                sender.addChatMessage(components.get(index));
            }
            else {
                sender.addChatMessage(new TextComponentString(""));
            }
        }
//cmd [Show-Chunks] <dim> <ticket> <page> <hash>
        ITextComponent start = new TextComponentString("=========").setStyle(new Style().setColor(TextFormatting.AQUA));
        ITextComponent prev = new TextComponentString(" <-Prev ").setStyle(new Style().setColor(TextFormatting.GOLD));

        LogHelper.error(page + " / " + pages);
        if (page > 0) {
            prev.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " [Show-Chunks] " + dimension + " " + ticketIndex + " " + (page - 1) + " " + hash));
        }

        ITextComponent pageDisplay = new TextComponentString(String.format("[Page: %s/%s]", page + 1, pages + 1));
        ITextComponent next = new TextComponentString(" Next-> ").setStyle(new Style().setColor(TextFormatting.GOLD));

        if (page < pages) {
            next.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + getCommandName() + " [Show-Chunks] " + dimension + " " + ticketIndex + " " + (page + 1) + " " + hash));
        }

        ITextComponent end = new TextComponentString("=========").setStyle(new Style().setColor(TextFormatting.AQUA));

        sender.addChatMessage(start.appendSibling(prev).appendSibling(pageDisplay).appendSibling(next).appendSibling(end));
    }
}
