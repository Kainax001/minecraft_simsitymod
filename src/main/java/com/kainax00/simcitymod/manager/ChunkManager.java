package com.kainax00.simcitymod.manager;

import net.minecraft.world.level.ChunkPos;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.info.ChunkInfo;

/**
 * Manages the logic for chunk data, including ownership and zone types.
 * This class handles in-memory data operations.
 */
public class ChunkManager {
    // Stores ChunkInfo (Owner + Type) mapped by the chunk's long position key.
    private static Map<Long, ChunkInfo> chunkData = new ConcurrentHashMap<>();

    /**
     * Sets the full information for a specific chunk.
     * Useful for admin commands to set specific zone types (e.g., Static Wilderness).
     *
     * @param pos   The position of the chunk.
     * @param owner The owner's UUID (can be null).
     * @param type  The zone type defined in ChunkType enum.
     */
    public static void setChunkInfo(ChunkPos pos, UUID owner, ChunkType type) {
        chunkData.put(pos.toLong(), new ChunkInfo(owner, type));
    }

    /**
     * Claims a chunk for a specific player.
     * This automatically sets the chunk type to RESIDENTIAL.
     *
     * @param pos   The position of the chunk.
     * @param owner The UUID of the player claiming the chunk.
     */
    public static void setOwner(ChunkPos pos, UUID owner) {
        chunkData.put(pos.toLong(), new ChunkInfo(owner, ChunkType.RESIDENTIAL));
    }

    /**
     * Removes all data associated with a chunk.
     * Effectively resets the chunk to the default state (Wilderness Reset).
     *
     * @param pos The position of the chunk to remove.
     */
    public static void removeChunkInfo(ChunkPos pos) {
        chunkData.remove(pos.toLong());
    }

    /**
     * Retrieves the owner of the chunk.
     *
     * @param pos The position of the chunk.
     * @return The UUID of the owner, or null if unowned or no data exists.
     */
    public static UUID getOwner(ChunkPos pos) {
        ChunkInfo info = chunkData.get(pos.toLong());
        return (info != null) ? info.owner : null;
    }

    /**
     * Retrieves the type of the chunk.
     * If no data exists for the chunk, it defaults to WILDERNESS_RESET.
     *
     * @param pos The position of the chunk.
     * @return The ChunkType of the chunk.
     */
    public static ChunkType getChunkType(ChunkPos pos) {
        ChunkInfo info = chunkData.get(pos.toLong());
        // Default to WILDERNESS_RESET if the chunk is not in the database
        return (info != null) ? info.type : ChunkType.WILDERNESS_RESET;
    }

    /**
     * Returns the entire map of chunk data.
     * Used primarily for saving data to JSON.
     *
     * @return The map containing all chunk data.
     */
    public static Map<Long, ChunkInfo> getAllChunkData() {
        return chunkData;
    }

    /**
     * Replaces the in-memory chunk data with loaded data.
     * Used primarily when loading data from JSON.
     *
     * @param loadedData The data map loaded from the file.
     */
    public static void setAllChunkData(Map<Long, ChunkInfo> loadedData) {
        chunkData = (loadedData != null) ? loadedData : new HashMap<>();
    }
}