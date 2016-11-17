package com.brandon3055.chunkmanager;

import com.brandon3055.chunkmanager.DataManager.LoadedChunk;
import com.brandon3055.chunkmanager.DataManager.UserData;
import com.brandon3055.chunkmanager.lib.LogHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class ChunkLoadingHandler {

    /** Stores a map of players to tickets. With tickets in a map of dimension to ticket. */
    public static Map<String, Map<Integer, Ticket>> playerTickets = new HashMap<>();
    /** This is just a list of all active tickets. */
    public static List<Ticket> ticketList = new LinkedList<>();

    public static void reloadChunks(MinecraftServer server) {
        for (Ticket ticket : ticketList) {
            ForgeChunkManager.releaseTicket(ticket);
        }

        playerTickets.clear();
        ticketList.clear();

        for (UserData data : DataManager.userDataList) {
            if (data.isPlayerOnline(server)) {
                updateLoading(data.username, server);
            }
        }
    }

    /**
     * Start loading all of this players chunks if the player is online.
     * This releases the player tickets if they exist then recreates them and loads the chunks.
     */
    public static void updateLoading(String username, MinecraftServer server) {
        LogHelper.dev("Updating user chunks for: " + username);

        if (playerTickets.containsKey(username)) {
            Map<Integer, Ticket> tickets = playerTickets.get(username);
            for (Ticket ticket : tickets.values()) {
                ticketList.remove(ticket);
                ForgeChunkManager.releaseTicket(ticket);
            }
            playerTickets.remove(username);
        }

        if (!playerTickets.containsKey(username)) {
            playerTickets.put(username, new HashMap<Integer, Ticket>());
        }

        Map<Integer, Ticket> tickets = playerTickets.get(username);

        UserData data = DataManager.getUserData(username);
        for (LoadedChunk chunk : data.chunksLoaded) {
            WorldServer world = server.worldServerForDimension(chunk.dimension);
            if (world == null) {
                LogHelper.warn("Could not load chunk in dimension " + chunk.dimension + " Did not find that dimension.");
                continue;
            }

            Ticket ticket;
            if (!tickets.containsKey(chunk.dimension)) {
                ticket = ForgeChunkManager.requestPlayerTicket(ChunkManager.instance, username, world, ForgeChunkManager.Type.NORMAL);
                if (ticket == null) {
                    LogHelper.warn("Could not get chunk loading ticket for player: " + username + " For dimension: " + chunk.dimension);
                    continue;
                }
                tickets.put(chunk.dimension, ticket);
                ticketList.add(ticket);
            }
            else {
                ticket = tickets.get(chunk.dimension);
            }

            ForgeChunkManager.forceChunk(ticket, chunk.getPos());
        }
    }

    /**
     * Stop loading all of this players chunks.
     */
    public static void stopLoading(String username, MinecraftServer server) {
        LogHelper.dev("Unloading user chunks for: " + username);
        if (playerTickets.containsKey(username)) {
            Map<Integer, Ticket> tickets = playerTickets.get(username);
            for (Ticket ticket : tickets.values()) {
                ForgeChunkManager.releaseTicket(ticket);
                ticketList.remove(ticket);
            }
            playerTickets.remove(username);
        }
    }
}
