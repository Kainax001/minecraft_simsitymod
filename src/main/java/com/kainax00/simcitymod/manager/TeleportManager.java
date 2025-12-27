package com.kainax00.simcitymod.manager;

import com.kainax00.simcitymod.data.ServerSpawnData;
import com.kainax00.simcitymod.util.IdentifierUtil;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Set;

public class TeleportManager {

    private static final int DEFAULT_X = 0;
    private static final int DEFAULT_Z = 0;

    /**
     * Pre-defined resource key for the custom wild dimension.
     */
    public static final ResourceKey<Level> WILD_DIMENSION_KEY = ResourceKey.create(
            Registries.DIMENSION,
            IdentifierUtil.create("simcitymod", "wild_dimension")
    );

    /**
     * Teleports the player to the wild dimension.
     */
    public static boolean teleportToWild(ServerPlayer player) {
        GlobalPos savedPos = ServerSpawnData.getWildSpawn(player.level().getServer());
        
        ServerLevel targetLevel;
        double targetX, targetY, targetZ;
        float targetYaw = player.getYRot();
        float targetPitch = player.getXRot();

        if (savedPos != null) {
            targetLevel = player.level().getServer().getLevel(savedPos.dimension());
            if (targetLevel == null) {
                player.displayClientMessage(Component.translatable("message.simcitymod.wild_load_error"), false);
                return false;
            }
            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            targetLevel = player.level().getServer().getLevel(WILD_DIMENSION_KEY);
            if (targetLevel == null) {
                player.displayClientMessage(Component.translatable("message.simcitymod.wild_load_error"), false);
                return false;
            }
            targetX = DEFAULT_X + 0.5;
            targetZ = DEFAULT_Z + 0.5;
            
            targetLevel.getChunkSource().getChunk((int)targetX >> 4, (int)targetZ >> 4, ChunkStatus.FULL, true);
            targetY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)targetX, (int)targetZ);
            if (targetY < targetLevel.getMinY()) targetY = 70;
            targetY += 1; 
        }

        player.teleportTo(targetLevel, targetX, targetY, targetZ, Set.of(), targetYaw, targetPitch, true);
        player.displayClientMessage(Component.translatable("message.simcitymod.teleport_wild_success"), true);
        return true;
    }

    /**
     * Teleports the player to the residential zone (Home).
     */
    public static boolean teleportToHome(ServerPlayer player) {
        GlobalPos savedPos = ServerSpawnData.getHomeSpawn(player.level().getServer());

        ServerLevel targetLevel;
        double targetX, targetY, targetZ;

        if (savedPos != null) {
            targetLevel = player.level().getServer().getLevel(savedPos.dimension());
            if (targetLevel == null) {
                player.displayClientMessage(Component.translatable("message.simcitymod.home_dimension_error"), false);
                return false;
            }
            targetX = savedPos.pos().getX() + 0.5;
            targetY = savedPos.pos().getY();
            targetZ = savedPos.pos().getZ() + 0.5;
        } else {
            targetLevel = player.level().getServer().getLevel(Level.OVERWORLD);
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