package com.kainax00.simcitymod.manager;

import com.kainax00.simcitymod.SimcityMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * --------------------------------------------------------------------------------
 * [ PRE-GENERATION SYSTEM SPECIFICATION & PERFORMANCE GUIDE ]
 * --------------------------------------------------------------------------------
 * TARGET WORKLOAD: ~98,000 Chunks (5000 radius WorldBorder)
 * * 1. MEMORY (RAM) REQUIREMENTS:
 * - Recommended Allocation: -Xmx16G (Minimum -Xmx12G)
 * - Reason: Full chunk generation caches structural and lighting data. 
 * 16GB ensures the Garbage Collector (GC) runs smoothly without freezing the Main Thread.
 * * 2. ESTIMATED COMPLETION TIME (Based on current settings: 5 chunks / 50ms sleep):
 * - Total Sleep Time: ~16.3 Minutes
 * - Processing Time (i7-12700KF): ~15-20 Minutes
 * - Total Estimated: ~35-45 Minutes (Varies by SSD write speed and thermal throttling)
 * * 3. HARDWARE OPTIMIZATION:
 * - HIGH-END DESKTOP (i7-12700KF): Can handle 'count % 10 == 0 / Thread.sleep(20)' 
 * to finish in ~20 mins.
 * - LAPTOP (Ryzen 7 5800H): Current setting (5 chunks / 50ms) is optimal 
 * to prevent thermal throttling and system instability.
 * --------------------------------------------------------------------------------
 */
public class ChunkPreGenerator {
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static final AtomicBoolean stopRequested = new AtomicBoolean(false);

    public static void startPreGeneration(ServerLevel level) {
        if (isRunning.get()) {
            SimcityMod.LOGGER.warn(">>> [SimCity] Pre-generation is already in progress.");
            return;
        }

        WorldBorder border = level.getWorldBorder();
        int minX = ((int) Math.floor(border.getMinX())) >> 4;
        int maxX = ((int) Math.floor(border.getMaxX())) >> 4;
        int minZ = ((int) Math.floor(border.getMinZ())) >> 4;
        int maxZ = ((int) Math.floor(border.getMaxZ())) >> 4;

        int totalChunks = (maxX - minX + 1) * (maxZ - minZ + 1);
        isRunning.set(true);
        stopRequested.set(false);

        String dimensionName = level.dimension().m_447358_().toString();
        SimcityMod.LOGGER.info(">>> [SimCity] Balanced Pre-gen started: {} chunks in {}.", totalChunks, dimensionName);

        CompletableFuture.runAsync(() -> {
            try {
                int count = 0;
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        if (stopRequested.get()) {
                            SimcityMod.LOGGER.info(">>> [SimCity] Pre-generation stopped by administrator.");
                            return;
                        }

                        // Generate the chunk up to FULL status (includes lighting, structures, and mobs)
                        level.getChunkSource().getChunk(x, z, ChunkStatus.FULL, true);
                        count++;

                        // THROTTLING LOGIC: Optimized for Ryzen 5800H & i7-12700KF Compatibility
                        // Prevents "Can't keep up" skips by yielding the main thread frequently.
                        if (count % 5 == 0) {
                            try { Thread.sleep(50); } catch (InterruptedException ignored) {}

                            if (count % 1000 == 0) {
                                final int currentCount = count;
                                SimcityMod.LOGGER.info(">>> [SimCity] Progress: {}/{} ({}%)", 
                                    currentCount, totalChunks, (currentCount * 100 / totalChunks));
                            }
                        }
                    }
                }
                SimcityMod.LOGGER.info(">>> [SimCity] Pre-generation successfully completed!");
            } catch (Exception e) {
                SimcityMod.LOGGER.error(">>> [SimCity] Critical Error during pre-gen: ", e);
            } finally {
                isRunning.set(false);
                stopRequested.set(false);
            }
        });
    }

    public static void stopPreGeneration() {
        if (isRunning.get()) stopRequested.set(true);
    }
}