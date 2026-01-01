package com.kainax00.simcitymod.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kainax00.simcitymod.data.info.HomeInfo;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * ServerSpawnData manages global spawn points for the server (Wild, Home, Nether, End).
 * Data is persisted in a JSON file within the world directory.
 */
public class ServerSpawnData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "simcity_server_spawn.json";
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Data structure for JSON serialization.
     * Fields must be public for GSON access.
     */
    public static class SpawnData {
        public HomeInfo wildSpawn;
        public HomeInfo homeSpawn;
        public HomeInfo netherSpawn;
        public HomeInfo endSpawn;
    }

    private static SpawnData data = null;

    /**
     * Resolves the file path for the spawn data JSON.
     */
    private static File getFile(MinecraftServer server) {
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        return worldPath.resolve(FILE_NAME).toFile();
    }

    // ==========================================
    // Setters (Triggers Save)
    // ==========================================

    /**
     * Sets the Wild dimension spawn point and saves to file.
     */
    public static void setWildSpawn(MinecraftServer server, GlobalPos pos) {
        ensureLoaded(server);
        data.wildSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    /**
     * Sets the Home dimension spawn point and saves to file.
     */
    public static void setHomeSpawn(MinecraftServer server, GlobalPos pos) {
        ensureLoaded(server);
        data.homeSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    /**
     * Sets the Nether dimension spawn point and saves to file.
     */
    public static void setNetherSpawn(MinecraftServer server, GlobalPos pos) {
        ensureLoaded(server);
        data.netherSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    /**
     * Sets the End dimension spawn point and saves to file.
     */
    public static void setEndSpawn(MinecraftServer server, GlobalPos pos) {
        ensureLoaded(server);
        data.endSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    // ==========================================
    // Getters
    // ==========================================

    public static GlobalPos getWildSpawn(MinecraftServer server) {
        ensureLoaded(server);
        return (data.wildSpawn != null) ? data.wildSpawn.toGlobalPos() : null;
    }

    public static GlobalPos getHomeSpawn(MinecraftServer server) {
        ensureLoaded(server);
        return (data.homeSpawn != null) ? data.homeSpawn.toGlobalPos() : null;
    }

    public static GlobalPos getNetherSpawn(MinecraftServer server) {
        ensureLoaded(server);
        return (data.netherSpawn != null) ? data.netherSpawn.toGlobalPos() : null;
    }

    public static GlobalPos getEndSpawn(MinecraftServer server) {
        ensureLoaded(server);
        return (data.endSpawn != null) ? data.endSpawn.toGlobalPos() : null;
    }

    // ==========================================
    // Internal Logic
    // ==========================================

    /**
     * Ensures the data instance is initialized and loaded from the file.
     */
    private static void ensureLoaded(MinecraftServer server) {
        if (data == null) {
            load(server);
        }
    }

    /**
     * Loads the spawn data from the JSON file.
     */
    private static void load(MinecraftServer server) {
        File file = getFile(server);
        if (!file.exists()) {
            data = new SpawnData();
            return;
        }

        try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            data = GSON.fromJson(reader, SpawnData.class);
            if (data == null) data = new SpawnData();
            LOGGER.info("[SimCityMod] Spawn data loaded successfully.");
        } catch (Exception e) {
            LOGGER.error("[SimCityMod] Failed to load spawn data", e);
            data = new SpawnData();
        }
    }

    /**
     * Serializes and saves the current spawn data to the JSON file.
     */
    private static void save(MinecraftServer server) {
        if (data == null) return;

        File file = getFile(server);
        
        // Ensure parent directories exist
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        // Write content to file using UTF-8 encoding
        try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
            String json = GSON.toJson(data);
            writer.write(json);
            LOGGER.info("[SimCityMod] Spawn data saved to file. Content: {}", json);
        } catch (IOException e) {
            LOGGER.error("[SimCityMod] Failed to save spawn data", e);
        }
    }
}