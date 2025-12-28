package com.kainax00.simcitymod.util;

import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PermissionUtil {
    
    public static boolean hasAdminPermission(Player player) {
        if (player == null) return false;

        PlayerInfo info = PlayerDataManager.getPlayerData(player);

        return info != null && info.getPermissionLevel() == PermissionLevel.ADMIN;
    }

    public static boolean hasAdminPermission(CommandSourceStack source) {
        if (!source.isPlayer()) {
            return true; 
        }

        if (source.getEntity() instanceof ServerPlayer serverPlayer) {
            return hasAdminPermission(serverPlayer);
        }

        return false;
    }
}