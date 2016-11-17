package com.brandon3055.chunkmanager.api;

/**
 * Created by brandon3055 on 17/11/2016.
 * This interface is used internally by ChunkManager and should not be implemented!
 */
public interface ICMRegistry {

    void registerListener(ICMListener listener);
}
