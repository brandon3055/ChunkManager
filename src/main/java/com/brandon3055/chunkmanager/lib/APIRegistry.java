package com.brandon3055.chunkmanager.lib;

import com.brandon3055.chunkmanager.api.ICMListener;
import com.brandon3055.chunkmanager.api.ICMRegistry;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by brandon3055 on 17/11/2016.
 */
public class APIRegistry implements ICMRegistry {

    private List<ICMListener> listeners = new LinkedList<>();

    @Override
    public void registerListener(ICMListener listener) {
        listeners.add(listener);
    }

    public int getBaseChunkAllowance(String username, int baseValue) {
        int currentValue = baseValue;

        for (ICMListener listener : listeners) {
            currentValue = listener.getBaseChunkAllowance(username, currentValue, baseValue);
        }

        return currentValue;
    }

    public int getTotalChunkAllowance(String username, int baseAllowance, int extraChunks) {
        int currentValue = baseAllowance + extraChunks;

        for (ICMListener listener : listeners) {
            currentValue = listener.getTotalChunkAllowance(username, currentValue, baseAllowance, extraChunks);
        }

        return currentValue;
    }

    public int getLogoutTimeout(String username, int baseValue) {
        int currentValue = baseValue;

        for (ICMListener listener : listeners) {
            currentValue = listener.getLogoutTimeout(username, currentValue, baseValue);
        }

        return currentValue;
    }
}
