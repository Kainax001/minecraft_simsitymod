package com.kainax00.simcitymod.data.info;

import com.kainax00.simcitymod.config.Config;
import com.kainax00.simcitymod.data.enums.PermissionLevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerInfo {
    private UUID uuid;
    private String playerName;
    private Set<UUID> friends;
    
    private int bonusLimit;
    private int baseLimit;
    private int maxLimit;
    
    private int claimedCount;
    private List<Long> claimedChunks;
    private PermissionLevel permissionLevel;

    private boolean canEnterNether;
    private boolean canEnterEnd;

    public PlayerInfo(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.friends = new HashSet<>();
        this.bonusLimit = 0;

        int currentBase = 4;
        if (Config.MAX_CLAIMS_PER_PLAYER != null) {
            currentBase = Config.MAX_CLAIMS_PER_PLAYER.get();
        }
        
        this.baseLimit = currentBase;
        this.maxLimit = currentBase + this.bonusLimit; 
        
        this.claimedCount = 0;
        this.claimedChunks = new ArrayList<>();
        this.permissionLevel = PermissionLevel.NONE;

        this.canEnterNether = false;
        this.canEnterEnd = false;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public Set<UUID> getFriends() { 
        if (this.friends == null) this.friends = new HashSet<>();
        return friends; 
    }
    public void setFriends(Set<UUID> friends) { this.friends = friends; }

    public int getBaseLimit() { 
        if (Config.MAX_CLAIMS_PER_PLAYER != null) {
            this.baseLimit = Config.MAX_CLAIMS_PER_PLAYER.get();
        }
        return baseLimit; 
    }
    public void setBaseLimit(int baseLimit) { this.baseLimit = baseLimit; }

    public int getBonusLimit() { return bonusLimit; }
    public void setBonusLimit(int bonusLimit) { this.bonusLimit = bonusLimit; }

    public int getMaxLimit() { 
        this.maxLimit = getBaseLimit() + this.bonusLimit;
        return maxLimit; 
    }
    public void setMaxLimit(int maxLimit) { this.maxLimit = maxLimit; }

    public int getClaimedCount() { return claimedCount; }
    public void setClaimedCount(int claimedCount) { this.claimedCount = claimedCount; }

    public List<Long> getClaimedChunks() {
        if (this.claimedChunks == null) this.claimedChunks = new ArrayList<>();
        return claimedChunks;
    }
    public void setClaimedChunks(List<Long> claimedChunks) { this.claimedChunks = claimedChunks; }

    public PermissionLevel getPermissionLevel() {
        if (this.permissionLevel == null) this.permissionLevel = PermissionLevel.NONE;
        return permissionLevel;
    }
    public void setPermissionLevel(PermissionLevel permissionLevel) { this.permissionLevel = permissionLevel; }

    public boolean canEnterNether() { return canEnterNether; }
    public void setCanEnterNether(boolean canEnterNether) { this.canEnterNether = canEnterNether; }

    public boolean canEnterEnd() { return canEnterEnd; }
    public void setCanEnterEnd(boolean canEnterEnd) { this.canEnterEnd = canEnterEnd; }
}