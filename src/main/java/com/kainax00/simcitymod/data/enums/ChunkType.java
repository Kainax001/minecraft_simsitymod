package com.kainax00.simcitymod.data.enums;

public enum ChunkType {
    WILDERNESS_RESET(0), // default type for unclaimed chunks
    WILDERNESS_STATIC(1), // wilderness that doesn't reset
    RESIDENTIAL(2); // claimed by players for building

    private final int id;
    ChunkType(int id) { this.id = id; }
    public int getId() { return id; }
}