package com.kainax00.simcitymod.util;

import net.minecraft.world.level.ChunkPos;
import java.util.HashSet;
import java.util.Set;

public class ChunkShapeUtil {

    public static Set<ChunkPos> getChunks(ShapeType shape, ChunkPos center, int radius) {
        switch (shape) {
            case SQUARE:
                return getSquareChunks(center, radius);
            case CIRCLE:
                return getCircleChunks(center, radius);
            default:
                return new HashSet<>();
        }
    }

    public static Set<ChunkPos> getSquareChunks(ChunkPos center, int radius) {
        Set<ChunkPos> chunks = new HashSet<>();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                chunks.add(new ChunkPos(center.x + x, center.z + z));
            }
        }
        return chunks;
    }

    public static Set<ChunkPos> getCircleChunks(ChunkPos center, int radius) {
        Set<ChunkPos> chunks = new HashSet<>();
        int radiusSq = radius * radius;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x * x + z * z <= radiusSq) {
                    chunks.add(new ChunkPos(center.x + x, center.z + z));
                }
            }
        }
        return chunks;
    }
}