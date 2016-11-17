package com.brandon3055.chunkmanager.api;

/**
 * Created by brandon3055 on 17/11/2016.
 * This api was added for lolnet.co.nz but anyone can use it.
 */
public class API {

    /**
     * This field should not be accessed directly! use the registerListener method.
     */
    @Deprecated
    public static ICMRegistry registry = null;

    /**
     * Use this to register an ICMListener for your mod. this must be registered after pre init. or after ChunkManager pre init.
     *
     * @param listener your mods ICMListener.
     */
    public static void registerListener(ICMListener listener) {
        if (registry != null) {
            registry.registerListener(listener);
        }
    }
}
