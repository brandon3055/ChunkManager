package com.brandon3055.chunkmanager;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class ModLoadingCallback implements ForgeChunkManager.LoadingCallback {

    @SideOnly(Side.SERVER) //TODO look into cleaning this up because this is probably not very efficient.
    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
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
