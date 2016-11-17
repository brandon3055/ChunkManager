package com.brandon3055.chunkmanager.lib;

import com.brandon3055.chunkmanager.api.CMPlugin;
import com.brandon3055.chunkmanager.api.IModPlugin;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import net.minecraftforge.fml.common.discovery.ASMDataTable.ASMData;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 17/11/2016.
 */
public class APIHelper {

    private static List<IModPlugin> pluginList = new LinkedList<>();

    public static void loadAPI(ASMDataTable table) {
        for (ASMData data : table.getAll(CMPlugin.class.getName())) {
            try {
                Class clazz = Class.forName(data.getClassName());
                if (IModPlugin.class.isAssignableFrom(clazz)) {
                    pluginList.add((IModPlugin) clazz.newInstance());
                }
                else {
                    throw new RuntimeException("Mod attempted to add CMPlugin annotation a class that dose not implement IModPlugin");
                }
            }
            catch (Exception e) {
                LogHelper.error("An error occurred while attempting to load mod plugin.");
                e.printStackTrace();
            }
        }
    }

    public static int getBaseChunkAllowance(String username, int baseValue) {
        int currentValue = baseValue;

        for (IModPlugin listener : pluginList) {
            currentValue = listener.getBaseChunkAllowance(username, currentValue, baseValue);
        }

        return currentValue;
    }

    public static int getTotalChunkAllowance(String username, int baseAllowance, int extraChunks) {
        int currentValue = baseAllowance + extraChunks;

        for (IModPlugin listener : pluginList) {
            currentValue = listener.getTotalChunkAllowance(username, currentValue, baseAllowance, extraChunks);
        }

        return currentValue;
    }

    public static int getLogoutTimeout(String username, int baseValue) {
        int currentValue = baseValue;

        for (IModPlugin listener : pluginList) {
            currentValue = listener.getLogoutTimeout(username, currentValue, baseValue);
        }

        return currentValue;
    }
}
