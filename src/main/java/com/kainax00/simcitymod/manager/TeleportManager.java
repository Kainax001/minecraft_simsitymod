package com.kainax00.simcitymod.manager;

import com.kainax00.simcitymod.data.MaintenanceData;
import com.kainax00.simcitymod.data.ServerSpawnData;
import com.kainax00.simcitymod.data.enums.SimDimensionType;
import com.kainax00.simcitymod.util.IdentifierUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Set;

/**
 * TeleportManager handles player transportation between dimensions.
 * It dynamically resolves the latest dimension version starting from v0.
 */
public class TeleportManager {

    private static final int DEFAULT_X = 0;
    private static final int DEFAULT_Z = 0;

    // =================================================================================
    // 1. WILD (Overworld Resource) Teleport Logic
    // =================================================================================

    /**
     * Teleports the player to the wild dimension.
     * It checks for a saved spawn point first; otherwise, it resolves the latest version (v0, v1...).
     */
    public static boolean teleportToWild(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        GlobalPos savedPos = ServerSpawnData.getWildSpawn(server);
        
        ServerLevel targetLevel;
        double targetX, targetY, targetZ;
        float targetYaw = player.getYRot();
        float targetPitch = player.getXRot();

        // 1. Try to use saved spawn point
        if (savedPos != null) {
            targetLevel = server.getLevel(savedPos.dimension());
            
            // Fallback to the latest version if the saved dimension version no longer exists
            if (targetLevel == null) {
                return teleportToLatestWild(player, server); 
            }

            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            // 2. No saved spawn, resolve current version (v0 by default)
            return teleportToLatestWild(player, server);
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), targetYaw, targetPitch, true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_wild_success"), true);
        return true;
    }

    private static boolean teleportToLatestWild(ServerPlayer player, MinecraftServer server) {
        int version = MaintenanceData.getCurrentVersion(server, SimDimensionType.WILD);
        String dimName = SimDimensionType.WILD.getIdPrefix() + "_v" + version;
        
        ResourceKey<Level> latestWildKey = ResourceKey.create(
            Registries.DIMENSION,
            IdentifierUtil.create("simcitymod", dimName)
        );

        ServerLevel targetLevel = server.getLevel(latestWildKey);
        
        if (targetLevel == null) {
            player.displayClientMessage(Component.literal("§c[Error] Wild dimension not found: " + dimName), false);
            return false;
        }

        double targetX = DEFAULT_X + 0.5;
        double targetZ = DEFAULT_Z + 0.5;

        // Ensure chunk is loaded to calculate safe height
        targetLevel.getChunkSource().getChunk((int)targetX >> 4, (int)targetZ >> 4, ChunkStatus.FULL, true);
        double targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)targetX, (int)targetZ);
        
        if (targetY < targetLevel.getMinY()) targetY = 70;
        targetY += 1;

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_wild_success"), true);
        return true;
    }

    // =================================================================================
    // 2. NETHER Teleport Logic
    // =================================================================================

    public static boolean teleportToNether(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        GlobalPos savedPos = ServerSpawnData.getNetherSpawn(server);

        ServerLevel targetLevel;
        double targetX, targetY, targetZ;
        float targetYaw = player.getYRot();
        float targetPitch = player.getXRot();

        if (savedPos != null) {
            targetLevel = server.getLevel(savedPos.dimension());
            if (targetLevel == null) {
                return teleportToLatestNether(player, server);
            }
            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            return teleportToLatestNether(player, server);
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), targetYaw, targetPitch, true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_nether_success"), true);
        return true;
    }

    private static boolean teleportToLatestNether(ServerPlayer player, MinecraftServer server) {
        int version = MaintenanceData.getCurrentVersion(server, SimDimensionType.NETHER);
        String dimName = SimDimensionType.NETHER.getIdPrefix() + "_v" + version;

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, IdentifierUtil.create("simcitymod", dimName));
        ServerLevel targetLevel = server.getLevel(key);

        if (targetLevel == null) {
            player.displayClientMessage(Component.literal("§c[Error] Nether dimension not found: " + dimName), false);
            return false;
        }

        // For Nether, default X/Z 0 is usually fine, but Y needs care.
        // If it's a custom dimension, we assume it's safe or we search.
        double targetX = DEFAULT_X + 0.5;
        double targetZ = DEFAULT_Z + 0.5;
        
        targetLevel.getChunkSource().getChunk((int)targetX >> 4, (int)targetZ >> 4, ChunkStatus.FULL, true);
        
        // Find a safe Y. In Nether, MOTION_BLOCKING might hit the roof (bedrock).
        // Simple safe spot strategy: Start from Y=50 and go up/down to find air.
        // For this snippet, we stick to a safe default if heightmap seems wrong (e.g. roof).
        double targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)targetX, (int)targetZ);
        
        // If targetY is too high (near roof) or invalid, default to a safe mid-level Y
        if (targetY > 120 || targetY < targetLevel.getMinY()) {
            targetY = 70; // Safe-ish middle ground
        } else {
            targetY += 1;
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_nether_success"), true);
        return true;
    }

    // =================================================================================
    // 3. END Teleport Logic
    // =================================================================================

    public static boolean teleportToEnd(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        GlobalPos savedPos = ServerSpawnData.getEndSpawn(server);

        ServerLevel targetLevel;
        double targetX, targetY, targetZ;
        float targetYaw = player.getYRot();
        float targetPitch = player.getXRot();

        if (savedPos != null) {
            targetLevel = server.getLevel(savedPos.dimension());
            if (targetLevel == null) {
                return teleportToLatestEnd(player, server);
            }
            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            return teleportToLatestEnd(player, server);
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), targetYaw, targetPitch, true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_end_success"), true);
        return true;
    }

    private static boolean teleportToLatestEnd(ServerPlayer player, MinecraftServer server) {
        int version = MaintenanceData.getCurrentVersion(server, SimDimensionType.END);
        String dimName = SimDimensionType.END.getIdPrefix() + "_v" + version;

        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, IdentifierUtil.create("simcitymod", dimName));
        ServerLevel targetLevel = server.getLevel(key);

        if (targetLevel == null) {
            player.displayClientMessage(Component.literal("§c[Error] End dimension not found: " + dimName), false);
            return false;
        }

        // Standard End spawn is usually at (0, Y, 0) on the obsidian platform
        double targetX = 0.5; // Center of 0,0
        double targetZ = 0.5;
        
        targetLevel.getChunkSource().getChunk(0, 0, ChunkStatus.FULL, true);
        double targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);

        if (targetY < targetLevel.getMinY()) targetY = 60; // Default End platform height
        targetY += 1;

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_end_success"), true);
        return true;
    }

    // =================================================================================
    // 4. HOME (Residential) Teleport Logic
    // =================================================================================

    /**
     * Teleports the player to the residential zone (Home).
     */
    public static boolean teleportToHome(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        GlobalPos savedPos = ServerSpawnData.getHomeSpawn(server);

        ServerLevel targetLevel;
        double targetX, targetY, targetZ;

        if (savedPos != null) {
            targetLevel = server.getLevel(savedPos.dimension());
            if (targetLevel == null) {
                player.displayClientMessage(Component.translatable("message.simcitymod.home_dimension_error"), false);
                return false;
            }
            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            // Default to Overworld if no home spawn is set
            targetLevel = server.getLevel(Level.OVERWORLD);
            if (targetLevel == null) return false;

            targetX = DEFAULT_X + 0.5;
            targetZ = DEFAULT_Z + 0.5;

            targetLevel.getChunkSource().getChunk((int)targetX >> 4, (int)targetZ >> 4, ChunkStatus.FULL, true);
            targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)targetX, (int)targetZ);
            if (targetY < targetLevel.getMinY()) targetY = 70;
            targetY += 1;
            
            player.displayClientMessage(Component.translatable("message.simcitymod.home_not_set_default"), false);
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), 0, 0, true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_home_success"), true);
        return true;
    }
}