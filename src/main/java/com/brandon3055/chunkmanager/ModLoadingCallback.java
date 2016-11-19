package com.brandon3055.chunkmanager;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class ModLoadingCallback implements ForgeChunkManager.LoadingCallback {

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        if (world.isRemote) {
            return;
        }
        List<String> toRefresh = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.isPlayerTicket()) {
                if (!toRefresh.contains(ticket.getPlayerName())) {
                    toRefresh.add(ticket.getPlayerName());
                }
            }
            ForgeChunkManager.releaseTicket(ticket);
        }

        for (String user : toRefresh) {
            ChunkLoadingHandler.updateLoading(user, world.getMinecraftServer());
        }
    }
}
