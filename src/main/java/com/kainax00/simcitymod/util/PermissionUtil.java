package com.kainax00.simcitymod.util;

import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

public class PermissionUtil {

    public static boolean hasAdminPermission(Player player) {
        if (player == null) return false;

        PlayerInfo info = PlayerDataManager.getPlayerData(player);

        if (info != null && info.getPermissionLevel() == PermissionLevel.ADMIN) {
            return true;
        }
        return false;
    }

    public static boolean hasAdminPermission(CommandSourceStack source) {
        if (source.getEntity() == null) {
            return true;
        }

        if (source.getEntity() instanceof Player player) {
            return hasAdminPermission(player);
        }

        return false;
    }
}