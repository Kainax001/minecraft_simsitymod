package com.kainax00.simcitymod.data.info;

import java.util.UUID;

import com.kainax00.simcitymod.data.enums.ChunkType;

/**
 * Data model representing the state of a single chunk.
 * Stores ownership information and the zone type.
 */
public class ChunkInfo {
    public UUID owner;
    public ChunkType type;

    public ChunkInfo() {
    }

    public ChunkInfo(UUID owner, ChunkType type) {
        this.owner = owner;
        this.type = type;
    }
}