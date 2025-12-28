package com.kainax00.simcitymod.data.info;

import com.kainax00.simcitymod.config.Config;
import com.kainax00.simcitymod.data.enums.PermissionLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerInfo {
    public UUID uuid;
    public String playerName;
    
    public int bonusLimit;
    public int maxLimit;
    
    public int claimedCount;
    public List<Long> claimedChunks;
    public PermissionLevel permissionLevel = PermissionLevel.NONE;

    public PlayerInfo(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.bonusLimit = 0;
        this.maxLimit = Config.MAX_CLAIMS_PER_PLAYER.get();
        this.claimedCount = 0;
        this.claimedChunks = new ArrayList<>();
        this.permissionLevel = PermissionLevel.NONE;
    }
}