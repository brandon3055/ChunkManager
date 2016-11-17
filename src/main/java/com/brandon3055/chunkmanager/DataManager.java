package com.brandon3055.chunkmanager;

import com.brandon3055.chunkmanager.lib.LogHelper;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by brandon3055 on 16/11/2016.
 */
public class DataManager {

    public static int logoutCoolDown = 0;
    public static int baseChunkAllocation = 0;

    private static File userData;
    private static File configFile;
    public static List<UserData> userDataList = new ArrayList<>();
    private static Map<String, UserData> nameToData = new HashMap<>();

    //region Init,Save,Load

    public static void initialize(File config) {
        File cManager = new File(config, "brandon3055/ChunkManager");
        if (!cManager.exists()) {
            cManager.mkdirs();
        }
        userData = new File(cManager, "UserData.json");
        configFile = new File(cManager, "Config.json");
        if (!configFile.exists() && !userData.exists()) {
            try {
                saveConfig();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            loadConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ChunkManager.configuration = new Configuration(new File(cManager, "ChunkManager.cfg"));
    }

    public static void saveConfig() throws IOException {
        LogHelper.dev("Saving: " + userDataList);

        JsonWriter writer = new JsonWriter(new FileWriter(configFile));
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("logoutCoolDown").value(logoutCoolDown);
        writer.name("baseChunkAllocation").value(baseChunkAllocation);
        writer.endObject();
        writer.close();

        writer = new JsonWriter(new FileWriter(userData));
        writer.setIndent("  ");
        writer.beginArray();
        for (UserData data : userDataList) {
            writer.beginObject();
            data.write(writer);
            writer.endObject();
        }
        writer.endArray();
        writer.close();
    }

    public static void loadConfig() throws IOException {
        JsonReader reader = new JsonReader(new FileReader(configFile));

        //region Config
        reader.beginObject();
        while (reader.hasNext()) {
            String next = reader.nextName();
            switch (next) {
                case "logoutCoolDown":
                    logoutCoolDown = reader.nextInt();
                    break;
                case "baseChunkAllocation":
                    baseChunkAllocation = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        //endregion

        userDataList.clear();
        nameToData.clear();
        reader = new JsonReader(new FileReader(userData));
        reader.beginArray();
        while (reader.hasNext()) {
            reader.beginObject();
            UserData data = new UserData();
            data.read(reader);
            userDataList.add(data);
            nameToData.put(data.username, data);
            reader.endObject();
        }
        reader.endArray();
    }

    //endregion

    //region Users

    public static UserData getUserData(String username) {
        if (!nameToData.containsKey(username)) {
            UserData data = new UserData();
            data.username = username;
            nameToData.put(username, data);
            userDataList.add(data);
        }

        return nameToData.get(username);
    }

    public static int getBaseChunkAllocation(String username) {
        return baseChunkAllocation;
    }

    public static int getUserChunkAllocation(String username) {
        return getBaseChunkAllocation(username) + getUserData(username).extraChunks;
    }

    public static int getUserLogoutCooldown(String username) {
        return logoutCoolDown;
    }

    //endregion

    //region Chunks

    public static boolean shouldLoadChunk() {
        return true;
    }

    //endregion

    //region Data Classes

    public static class UserData {
        public String username = "";
        /**
         * The number of extraChunks this player is allowed to load
         */
        public int extraChunks = 0;
        /**
         * List of chunks this player is currently loading
         */
        public LinkedList<LoadedChunk> chunksLoaded = new LinkedList<>();

        public void write(JsonWriter writer) throws IOException {
            writer.name("username").value(username);
            writer.name("extraChunks").value(extraChunks);
            writer.name("loaded");
            writer.beginArray();
            for (LoadedChunk chunk : chunksLoaded) {
                writer.value("x:" + chunk.chunkX + "z:" + chunk.chunkZ + "d:" + chunk.dimension);
            }
            writer.endArray();
        }

        public void read(JsonReader reader) throws IOException {
            while (reader.hasNext()) {
                String next = reader.nextName();
                switch (next) {
                    case "username":
                        username = reader.nextString();
                        break;
                    case "extraChunks":
                        extraChunks = reader.nextInt();
                        break;
                    case "loaded":
                        reader.beginArray();
                        while (reader.hasNext()) {
                            LoadedChunk chunk = new LoadedChunk();
                            String c = reader.nextString();
                            try {
                                chunk.chunkX = Integer.parseInt(c.substring(c.indexOf("x:") + 2, c.indexOf("z:")));
                                chunk.chunkZ = Integer.parseInt(c.substring(c.indexOf("z:") + 2, c.indexOf("d:")));
                                chunk.dimension = Integer.parseInt(c.substring(c.indexOf("d:") + 2));
                            }
                            catch (Exception e) {
                                LogHelper.error("Error Parsing Chunk Pos - " + c);
                                e.printStackTrace();
                            }
                            chunksLoaded.add(chunk);
                        }
                        reader.endArray();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            int i = 0;
            while (chunksLoaded.size() > getBaseChunkAllocation(username) + extraChunks && i++ < 1000) {
                chunksLoaded.removeLast();
            }
        }

        public boolean isPlayerOnline(MinecraftServer server) {
            return server.getPlayerList().getPlayerByUsername(username) != null;
        }
    }

    public static class LoadedChunk {
        public int chunkX;
        public int chunkZ;
        public int dimension;

        public LoadedChunk() {}

        public LoadedChunk(int chunkX, int chunkZ, int dimension) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.dimension = dimension;
        }

        public static LoadedChunk fromPlayer(EntityPlayer player) {
            Chunk chunk = player.worldObj.getChunkFromBlockCoords(player.getPosition());
            return new LoadedChunk(chunk.xPosition, chunk.zPosition, player.worldObj.provider.getDimension());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof LoadedChunk)) {
                return false;
            }
            LoadedChunk c = (LoadedChunk) obj;
            return c.chunkX == chunkX && c.chunkZ == chunkZ && c.dimension == dimension;
        }

        public ChunkPos getPos() {
            return new ChunkPos(chunkX, chunkZ);
        }
    }

    //endregion

    public static String getFormattedCooldown() {
        int totalSec = logoutCoolDown / 20;
        int hours = (totalSec / 3600) % 24;
        int mins = (totalSec / 60) % 60;
        int secs = totalSec % 60;

        return String.format("[%s:%s:%s]", (hours < 10 ? "0" : "") + hours, (mins < 10 ? "0" : "") + mins, (secs < 10 ? "0" : "") + secs);
    }
}
