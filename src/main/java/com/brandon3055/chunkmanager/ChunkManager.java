package com.brandon3055.chunkmanager;

import com.brandon3055.chunkmanager.commands.CommandChunkload;
import com.brandon3055.chunkmanager.commands.CommandManage;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Map;


@Mod(modid = ChunkManager.MODID, name = ChunkManager.MODNAME, version = ChunkManager.VERSION)
public class ChunkManager {
    public static final String MODID = "chunkmanager";
    public static final String MODNAME = "Chunk Manager";
    public static final String VERSION = "${mod_version}";
    public static Configuration configuration;
    public static String chunkloadCommand = "chunkload";
    public static String chunkManagerCommand = "chunkmanager";

    @Mod.Instance(ChunkManager.MODID)
    public static ChunkManager instance;

    @NetworkCheckHandler
    public boolean networkCheck(Map<String, String> map, Side side) {
        return true;
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandChunkload());
        event.registerServerCommand(new CommandManage());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        DataManager.initialize(event.getModConfigurationDirectory());
        ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ModLoadingCallback());
        MinecraftForge.EVENT_BUS.register(new ModEventHandler());

        try {
            chunkloadCommand = configuration.get(Configuration.CATEGORY_GENERAL, "chunkloadCommand", chunkloadCommand, "This allows you to change the name of the chunkload command.").getString();
            chunkManagerCommand = configuration.get(Configuration.CATEGORY_GENERAL, "chunkManagerCommand", chunkManagerCommand, "This allows you to change the name of the chunkmanager command.").getString();

            if (configuration.hasChanged()) {
                configuration.save();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }
}
