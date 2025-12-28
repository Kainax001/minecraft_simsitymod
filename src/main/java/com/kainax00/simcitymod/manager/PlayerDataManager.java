package com.kainax00.simcitymod.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.ChunkInfo;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.config.Config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;

public class PlayerDataManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<UUID, PlayerInfo> players = new HashMap<>();

    /**
     * Path for individual player statistics (limits, claimed count, and chunk lists).
     * Location: world/simcity_player_data.json
     */
    private static Path getPlayersPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("simcity_player_data.json");
    }
    
    /**
     * Path for global chunk data (ownership and zone types).
     * Location: world/simcity_claims.json
     */
    private static Path getClaimsPath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("simcity_claims.json");
    }

    // ==========================================
    // Event Handlers
    // ==========================================

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        loadAll(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        saveAll(event.getServer());
    }

    @SubscribeEvent
    public static void onWorldSave(net.minecraftforge.event.level.LevelEvent.Save event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            saveAll(serverLevel.getServer());
        }
    }

    // ==========================================
    // Core Logic 
    // ==========================================

    public static void loadAll(MinecraftServer server) {
        if (server == null) return;
        loadPlayers(server);
        loadClaims(server);
    }

    public static void saveAll(MinecraftServer server) {
        if (server == null) return;
        savePlayers(server);
        saveClaims(server);
    }

    private static void loadPlayers(MinecraftServer server) {
        Path path = getPlayersPath(server);
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path)) {
            Type type = new TypeToken<HashMap<UUID, PlayerInfo>>(){}.getType();
            players = GSON.fromJson(reader, type);
            if (players == null) players = new HashMap<>();

            for (PlayerInfo info : players.values()) {
                if (info.getPermissionLevel() == null) {
                    info.setPermissionLevel(PermissionLevel.NONE);
                }
                if (info.getFriends() == null) {
                    info.setFriends(new HashSet<>());
                }
            }
        } catch (IOException e) {
            SimcityMod.LOGGER.error("Failed to load player data", e);
        }
    }

    private static void loadClaims(MinecraftServer server) {
        Path path = getClaimsPath(server);
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path)) {
            Type type = new TypeToken<HashMap<Long, ChunkInfo>>(){}.getType();
            Map<Long, ChunkInfo> loadedData = GSON.fromJson(reader, type);
            ChunkManager.setAllChunkData(loadedData);
        } catch (IOException e) {
            SimcityMod.LOGGER.error("Failed to load global chunk data", e);
        }
    }

    private static void savePlayers(MinecraftServer server) {
        try (Writer writer = Files.newBufferedWriter(getPlayersPath(server))) {
            GSON.toJson(players, writer);
        } catch (IOException e) {
            SimcityMod.LOGGER.error("Failed to save player data", e);
        }
    }

    private static void saveClaims(MinecraftServer server) {
        try (Writer writer = Files.newBufferedWriter(getClaimsPath(server))) {
            GSON.toJson(ChunkManager.getAllChunkData(), writer);
        } catch (IOException e) {
            SimcityMod.LOGGER.error("Failed to save global chunk data", e);
        }
    }

    public static int getTotalClaimLimit(PlayerInfo info) {
        if (info == null) return Config.MAX_CLAIMS_PER_PLAYER.get();
        return Config.MAX_CLAIMS_PER_PLAYER.get() + info.getBonusLimit();
    }

    public static PlayerInfo getPlayerData(Player player) {
        return getOrCreateData(player.getUUID(), player.getName().getString());
    }

    public static PlayerInfo getOrCreateData(UUID uuid, String name) {
        return players.computeIfAbsent(uuid, k -> new PlayerInfo(uuid, name));
    }
    
    public static PlayerInfo getPlayerData(UUID uuid) {
        return players.get(uuid);
    }

    public static String getPlayerName(UUID uuid) {
        if (uuid == null) return "None";
        PlayerInfo data = players.get(uuid); 
        if (data != null && data.getPlayerName() != null) {
            return data.getPlayerName();
        }
        return "Unknown (" + uuid.toString().substring(0, 8) + ")";
    }

    // ==========================================
    // Friend Management Logic
    // ==========================================

    public static boolean addFriend(UUID ownerUUID, UUID friendUUID, MinecraftServer server) {
        PlayerInfo info = getOrCreateData(ownerUUID, "Unknown");
        if (info.getFriends().contains(friendUUID)) {
            return false;
        }
        info.getFriends().add(friendUUID);
        return true;
    }

    public static boolean removeFriend(UUID ownerUUID, UUID friendUUID, MinecraftServer server) {
        PlayerInfo info = players.get(ownerUUID);
        if (info != null && info.getFriends().contains(friendUUID)) {
            info.getFriends().remove(friendUUID);
            return true;
        }
        return false;
    }

    public static boolean isFriend(UUID ownerUUID, UUID visitorUUID) {
        PlayerInfo info = players.get(ownerUUID);
        return info != null && info.getFriends().contains(visitorUUID);
    }

    public static Set<UUID> getFriendList(UUID ownerUUID) {
        PlayerInfo info = players.get(ownerUUID);
        if (info != null) {
            return info.getFriends();
        }
        return new HashSet<>();
    }
}