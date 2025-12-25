package com.kainax00.simcitymod.config;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

/**
 * Mapping class for obfuscated permission fields in Minecraft 1.21.1.
 * This class provides readable constants for different permission levels and sets.
 */
public class PermissionNodes {

    // --- Single Permission Nodes ---

    public static final Permission MODERATORS = Permissions.f_434428_;
    public static final Permission GAMEMASTERS = Permissions.f_433632_;
    public static final Permission ADMINS = Permissions.f_434275_;
    public static final Permission OWNERS = Permissions.f_433869_;
    public static final Permission ENTITY_SELECTORS = Permissions.f_434653_;

    // --- Permission Sets (Minimum Level Required) ---
    // These constants include the specified level and ALL levels above it.

    /**
     * Includes MODERATORS, GAMEMASTERS, ADMINS, and OWNERS.
     */
    public static final PermissionSet AT_LEAST_MODERATORS = LevelBasedPermissionSet.f_433747_;

    /**
     * Includes GAMEMASTERS, ADMINS, and OWNERS.
     */
    public static final PermissionSet AT_LEAST_GAMEMASTERS = LevelBasedPermissionSet.f_433961_;

    /**
     * Includes ADMINS and OWNERS.
     */
    public static final PermissionSet AT_LEAST_ADMINS = LevelBasedPermissionSet.f_436361_;

    /**
     * Only Server OWNERS.
     */
    public static final PermissionSet AT_LEAST_OWNERS = LevelBasedPermissionSet.f_435420_;
}