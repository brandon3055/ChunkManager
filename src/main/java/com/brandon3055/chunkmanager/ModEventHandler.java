package com.brandon3055.chunkmanager;

import com.brandon3055.chunkmanager.DataManager.LoadedChunk;
import com.brandon3055.chunkmanager.DataManager.UserData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

/**
 * Created by brandon3055 on 11/11/2016.
 */
public class ModEventHandler {

    public static List<String> chunkDisplay = new ArrayList<>();
    public static Map<String, Integer> scheduledUnloads = new HashMap<>();

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void playerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.side == Side.CLIENT) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;
        if (chunkDisplay.contains(player.getName())) {
            UserData data = DataManager.getUserData(player.getName());
            Random rand = player.worldObj.rand;
            for (LoadedChunk chunk : data.chunksLoaded) {
                if (player.worldObj.provider.getDimension() == chunk.dimension) {
                    for (int i = 0; i < 64; i++) {
                        if (rand.nextInt(16) == 0 && player.worldObj.isAirBlock(new BlockPos((chunk.chunkX * 16) + 8, i * 4, (chunk.chunkZ * 16) + 8))) {
                            ((WorldServer) player.worldObj).spawnParticle(player, EnumParticleTypes.DRAGON_BREATH, true, (chunk.chunkX * 16) + 8, i * 4, (chunk.chunkZ * 16) + 8, 1, 0, 0, 0, 0.05, 0);
                        }
                    }
                }
            }
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void playerLogin(PlayerLoggedInEvent event) {
        if (event.player.worldObj instanceof WorldServer) {
            WorldServer world = (WorldServer) event.player.worldObj;
            if (scheduledUnloads.containsKey(event.player.getName())) {
                scheduledUnloads.remove(event.player.getName());
            }
            ChunkLoadingHandler.updateLoading(event.player.getName(), world.getMinecraftServer());
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.worldObj instanceof WorldServer) {
            scheduledUnloads.put(event.player.getName(), DataManager.getUserLogoutCooldown(event.player.getName()));
        }
    }

    @SideOnly(Side.SERVER)
    @SubscribeEvent
    public void serverTickEvent(ServerTickEvent event) {
        for (String user : scheduledUnloads.keySet()) {
            scheduledUnloads.put(user, scheduledUnloads.get(user) - 1);

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (scheduledUnloads.get(user) <= 0 && server != null) {
                ChunkLoadingHandler.stopLoading(user, server);
                scheduledUnloads.remove(user);
            }
            else if (server == null) {
                LogHelper.error("Could not unload player chunks because server is null?!?!");
            }
        }
    }
}