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

    public static boolean teleportToWild(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        GlobalPos savedPos = ServerSpawnData.getWildSpawn(server);
        
        ServerLevel targetLevel;
        double targetX, targetY, targetZ;

        if (savedPos != null) {
            targetLevel = server.getLevel(savedPos.dimension());
            if (targetLevel == null) {
                player.displayClientMessage(Component.translatable("message.simcitymod.wild_not_found_default"), false);
                return teleportToLatestWild(player, server); 
            }
            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            player.displayClientMessage(Component.translatable("message.simcitymod.wild_not_set_default"), false);
            return teleportToLatestWild(player, server);
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_wild_success"), true);
        return true;
    }

    private static boolean teleportToLatestWild(ServerPlayer player, MinecraftServer server) {
        int version = MaintenanceData.getCurrentVersion(server, SimDimensionType.WILD);
        String dimName = SimDimensionType.WILD.getIdPrefix() + "_v" + version;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, IdentifierUtil.create("simcitymod", dimName));
        ServerLevel targetLevel = server.getLevel(key);
        
        if (targetLevel == null) return false;

        double targetX = DEFAULT_X + 0.5;
        double targetZ = DEFAULT_Z + 0.5;
        targetLevel.getChunkSource().getChunk((int)targetX >> 4, (int)targetZ >> 4, ChunkStatus.FULL, true);
        double targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)targetX, (int)targetZ);
        if (targetY < targetLevel.getMinY()) targetY = 70;
        targetY += 1;

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);
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

        if (savedPos != null) {
            targetLevel = server.getLevel(savedPos.dimension());
            if (targetLevel == null) {
                player.displayClientMessage(Component.translatable("message.simcitymod.nether_not_found_default"), false);
                return teleportToLatestNether(player, server);
            }
            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            player.displayClientMessage(Component.translatable("message.simcitymod.nether_not_set_default"), false);
            return teleportToLatestNether(player, server);
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_nether_success"), true);
        return true;
    }

    private static boolean teleportToLatestNether(ServerPlayer player, MinecraftServer server) {
        int version = MaintenanceData.getCurrentVersion(server, SimDimensionType.NETHER);
        String dimName = SimDimensionType.NETHER.getIdPrefix() + "_v" + version;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, IdentifierUtil.create("simcitymod", dimName));
        ServerLevel targetLevel = server.getLevel(key);

        if (targetLevel == null) return false;

        double targetX = DEFAULT_X + 0.5;
        double targetZ = DEFAULT_Z + 0.5;
        targetLevel.getChunkSource().getChunk((int)targetX >> 4, (int)targetZ >> 4, ChunkStatus.FULL, true);
        double targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)targetX, (int)targetZ);
        if (targetY > 120 || targetY < targetLevel.getMinY()) targetY = 70;
        else targetY += 1;

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);
        return true;
    }

    // =================================================================================
    // 3. END Teleport Logic (Integrated)
    // =================================================================================

    public static boolean teleportToEnd(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        GlobalPos savedPos = ServerSpawnData.getEndSpawn(server);

        ServerLevel targetLevel = null;
        double targetX = 0, targetY = 0, targetZ = 0;
        boolean useFallback = false;

        if (savedPos != null) {
            targetLevel = server.getLevel(savedPos.dimension());
            if (targetLevel == null) {
                player.displayClientMessage(Component.translatable("message.simcitymod.end_not_found_default"), false);
                useFallback = true;
            } else {
                targetX = savedPos.pos().getX() + 0.5;
                targetY = savedPos.pos().getY();
                targetZ = savedPos.pos().getZ() + 0.5;
            }
        } else {
            player.displayClientMessage(Component.translatable("message.simcitymod.end_not_set_default"), false);
            useFallback = true;
        }

        if (useFallback) {
            targetLevel = server.getLevel(Level.END);
            if (targetLevel == null) return false;

            targetX = 0.5;
            targetZ = 0.5;

            targetLevel.getChunkSource().getChunk(0, 0, ChunkStatus.FULL, true);
            targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, 0, 0);
            
            if (targetY < targetLevel.getMinY()) targetY = 60;
            targetY += 1;
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), player.getYRot(), player.getXRot(), true);

        if (!useFallback) {
            player.displayClientMessage(Component.translatable("message.simcitymod.teleport_end_success"), true);
        }
        
        return true;
    }

    // =================================================================================
    // 4. HOME (Residential) Teleport Logic
    // =================================================================================

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