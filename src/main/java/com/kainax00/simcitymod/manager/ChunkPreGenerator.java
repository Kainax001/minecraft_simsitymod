
package com.kainax00.simcitymod.manager;

import com.kainax00.simcitymod.SimcityMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * [ System Optimization Notes ]
 * * Target Hardware: AMD Ryzen 7 5800H (8-Cores / 16-Threads), 32GB DDR4 RAM.
 * * 1. CPU Utilization (5800H):
 * - Increased the skipping threshold for pre-generated chunks to 200 cycles.
 * - This reduces unnecessary thread yielding (Sleep) and leverages high multi-threaded performance.
 * * 2. Memory Management (32GB RAM):
 * - Chunk cleanup threshold is set to 25,000 loaded chunks.
 * - Provides a balance between preventing server lag and utilizing the large 16GB-32GB allocated heap.
 * - Manual System.gc() is triggered after deep cleaning cycles to maintain heap health.
 * * 3. I/O Optimization:
 * - saveAll() uses deferred flushing (flush=false) during active generation to minimize NVMe SSD write bottleneck.
 */
public class ChunkPreGenerator {
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    private static final AtomicBoolean stopRequested = new AtomicBoolean(false);

    private static final int REGION_SIZE = 32;

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
        SimcityMod.LOGGER.info(">>> [SimCity] HIGH-PERFORMANCE PRE-GEN STARTED: {} chunks in {}.", totalChunks, dimensionName);

        CompletableFuture.runAsync(() -> {
            try {
                int count = 0;
                int skippedInBatch = 0;
                long lastTime = System.currentTimeMillis();
                var chunkSource = level.getChunkSource();

                for (int rX = minX; rX <= maxX; rX += REGION_SIZE) {
                    for (int rZ = minZ; rZ <= maxZ; rZ += REGION_SIZE) {
                        
                        if (stopRequested.get()) {
                            saveAll(level, true);
                            SimcityMod.LOGGER.info(">>> [SimCity] Stopped by user.");
                            return;
                        }

                        int limitX = Math.min(rX + REGION_SIZE, maxX + 1);
                        int limitZ = Math.min(rZ + REGION_SIZE, maxZ + 1);

                        for (int x = rX; x < limitX; x++) {
                            for (int z = rZ; z < limitZ; z++) {
                                
                                if (stopRequested.get()) return;

                                final int cx = x;
                                final int cz = z;

                                boolean isNewGen = level.getServer().submit(() -> {
                                    ChunkAccess chunk = chunkSource.getChunk(cx, cz, ChunkStatus.EMPTY, true);
                                    if (chunk != null && chunk.getPersistedStatus().isOrAfter(ChunkStatus.FULL)) {
                                        return false;
                                    }
                                    chunkSource.getChunk(cx, cz, ChunkStatus.FULL, true);
                                    return true;
                                }).join();

                                if (!isNewGen) skippedInBatch++;
                                count++;

                                if (!isNewGen && count % 200 == 0) {
                                     try { Thread.sleep(1); } catch (Exception e) {}
                                }

                                if (count % 2000 == 0) {
                                    long currentTime = System.currentTimeMillis();
                                    double timeSeconds = (currentTime - lastTime) / 1000.0;
                                    double cps = 2000.0 / Math.max(0.1, timeSeconds);
                                    double progressPercent = ((double) count / totalChunks) * 100.0;
                                    
                                    Runtime rt = Runtime.getRuntime();
                                    long usedMemMB = (rt.totalMemory() - rt.freeMemory()) / 1024 / 1024;
                                    int loadedChunks = chunkSource.getLoadedChunksCount();

                                    String statusMode = (skippedInBatch > 1900) ? "FAST-SKIPPING" : "GENERATING";

                                    SimcityMod.LOGGER.info(String.format(
                                        ">>> [SimCity] %s | %d/%d (%.2f%%) | Speed: %.1f ch/s | RAM: %d MB | Loaded: %d",
                                        statusMode, count, totalChunks, progressPercent, 
                                        cps, usedMemMB, loadedChunks
                                    ));

                                    lastTime = currentTime;
                                    skippedInBatch = 0;
                                }
                            }
                        } 

                        performDeepCleanCycle(level, chunkSource);
                    }
                } 
                
                SimcityMod.LOGGER.info(">>> [SimCity] ALL DONE! Dimension {} is fully pre-generated.", dimensionName);
            } catch (Exception e) {
                SimcityMod.LOGGER.error(">>> [SimCity] Fatal error during pre-generation: ", e);
            } finally {
                isRunning.set(false);
                stopRequested.set(false);
            }
        });
    }

    public static void stopPreGeneration() {
        if (isRunning.get()) stopRequested.set(true);
    }

    private static void saveAll(ServerLevel level, boolean flush) {
        level.getServer().submit(() -> level.getChunkSource().save(flush)).join();
    }

    private static void performDeepCleanCycle(ServerLevel level, net.minecraft.server.level.ServerChunkCache chunkSource) {
        saveAll(level, false);

        if (chunkSource.getLoadedChunksCount() > 25000) {
            SimcityMod.LOGGER.info(">>> [SimCity] [CLEANER] High chunk count detected ({}), purging memory...", chunkSource.getLoadedChunksCount());

            for (int i = 0; i < 3; i++) {
                level.getServer().submit(() -> {
                    chunkSource.tick(() -> true, false); 
                });
                try { Thread.sleep(100); } catch (Exception e) {}
            }

            System.gc();
            
            SimcityMod.LOGGER.info(">>> [SimCity] [CLEANER] Cleanup finished. Loaded Chunks: " + chunkSource.getLoadedChunksCount());
        }
    }
}