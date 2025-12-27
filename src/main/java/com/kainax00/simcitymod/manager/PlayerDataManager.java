package com.kainax00.simcitymod.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.ChunkInfo;
import com.kainax00.simcitymod.data.info.PlayerInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
                if (info.permissionLevel == null) {
                    info.permissionLevel = PermissionLevel.NONE;
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

    public static PlayerInfo getPlayerData(Player player) {
        return getOrCreateData(player.getUUID(), player.getName().getString());
    }

    public static PlayerInfo getOrCreateData(UUID uuid, String name) {
        return players.computeIfAbsent(uuid, k -> new PlayerInfo(uuid, name));
    }

    public static String getPlayerName(UUID uuid) {
        if (uuid == null) return "None";

        PlayerInfo data = players.get(uuid); 

        if (data != null && data.playerName != null) {
            return data.playerName;
        }

        return "Unknown (" + uuid.toString().substring(0, 8) + ")";
    }
}