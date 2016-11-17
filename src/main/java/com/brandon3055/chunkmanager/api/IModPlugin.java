package com.brandon3055.chunkmanager.api;

/**
 * Created by brandon3055 on 17/11/2016.
 * This api was added for lolnet.co.nz but anyone can use it.
 *
 * To use this interface simply add it to a class and implement the annotation CMPlugin on that class.
 * ChunkManager will than find the class on startup, create a new instance of it and call its methods automatically.
 */
public interface IModPlugin {
    /**
     * This allows you to modify the base chunk allocation for each player. This will be called whenever anything needs to check the players base chunk allocation.
     * You can use this for example to add extra chunks to players with different ranks.
     * If you are not using this simply return currentValue.
     *
     * @param username The players username
     * @param currentValue The current base player chunk allocation. This will be the same as baseValue if it has not been modified by another ICSGListener.
     * @param baseValue The base chunk allocation given to all players.
     * @return A new base chunk allocation for this player.
     */
    int getBaseChunkAllowance(String username, int currentValue, int baseValue);

    /**
     * This allows you to modify the total chunk allowance for a player. In most cases you should just be able to use getBaseChunkAllowance for this but this is here if you need it.
     * If you are not using this simply return currentValue.
     *
     * @param username The players username.
     * @param currentValue The current total chunk allowance for this player.
     * @param baseAllowance The base allowance for this player. This will return whatever you return in getBaseChunkAllowance assuming the value has not been changed another ICSGListener.
     * @param extraChunks this is the number of extra chunks given to this player via the /chunkmanager add command.
     * @return a new chunk allowance for this player.
     */
    int getTotalChunkAllowance(String username, int currentValue, int baseAllowance, int extraChunks);

    /**
     * This allows you to modify the logout timeout for each player. This will be called whenever a player disconnects.
     *
     * @param username The players username
     * @param currentValue The current player logout timeout. This will be the same as baseValue if it has not been modified by another ICSGListener.
     * @param baseValue this is the base logout timeout for all players.
     * @return a new logout timeout for this player.
     */
    int getLogoutTimeout(String username, int currentValue, int baseValue);
}
