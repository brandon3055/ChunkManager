package com.brandon3055.chunkmanager;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.List;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class ModLoadingCallback implements ForgeChunkManager.LoadingCallback {

    @Override
    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
        LogHelper.dev("Callback");
    }
}
