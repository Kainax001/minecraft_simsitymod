package com.kainax00.simcitymod.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kainax00.simcitymod.data.info.HomeInfo;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * ServerSpawnData manages the global spawn points for the server (Wild, Home, Nether, End).
 * Data is persisted in a JSON file within the world directory.
 */
public class ServerSpawnData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "simcity_server_spawn.json";

    /**
     * Internal data structure for JSON serialization.
     */
    private static class SpawnData {
        HomeInfo wildSpawn;
        HomeInfo homeSpawn;
        HomeInfo netherSpawn; // Added
        HomeInfo endSpawn;    // Added
    }

    /**
     * The data instance is initialized as null to ensure load(server) is called 
     * upon the first access, preventing data loss after server restarts.
     */
    private static SpawnData data = null;

    /**
     * Resolves the path to the JSON storage file.
     */
    private static File getFile(MinecraftServer server) {
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        return worldPath.resolve(FILE_NAME).toFile();
    }

    // ==========================================
    // 1. Wild Spawn Methods
    // ==========================================

    /**
     * Sets the global wild spawn point and saves to file.
     */
    public static void setWildSpawn(MinecraftServer server, GlobalPos pos) {
        if (data == null) load(server);
        
        data.wildSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    /**
     * Retrieves the global wild spawn point, loading from file if necessary.
     */
    public static GlobalPos getWildSpawn(MinecraftServer server) {
        if (data == null) load(server);

        if (data != null && data.wildSpawn != null) {
            return data.wildSpawn.toGlobalPos();
        }
        return null; 
    }

    // ==========================================
    // 2. Home Spawn Methods
    // ==========================================

    /**
     * Sets the global residential (home) spawn point and saves to file.
     */
    public static void setHomeSpawn(MinecraftServer server, GlobalPos pos) {
        if (data == null) load(server);
        
        data.homeSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    /**
     * Retrieves the global home spawn point, loading from file if necessary.
     */
    public static GlobalPos getHomeSpawn(MinecraftServer server) {
        if (data == null) load(server);

        if (data != null && data.homeSpawn != null) {
            return data.homeSpawn.toGlobalPos();
        }
        return null; 
    }

    // ==========================================
    // 3. Nether Spawn Methods
    // ==========================================

    /**
     * Sets the global nether spawn point and saves to file.
     */
    public static void setNetherSpawn(MinecraftServer server, GlobalPos pos) {
        if (data == null) load(server);

        data.netherSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    /**
     * Retrieves the global nether spawn point.
     */
    public static GlobalPos getNetherSpawn(MinecraftServer server) {
        if (data == null) load(server);

        if (data != null && data.netherSpawn != null) {
            return data.netherSpawn.toGlobalPos();
        }
        return null;
    }

    // ==========================================
    // 4. End Spawn Methods
    // ==========================================

    /**
     * Sets the global end spawn point and saves to file.
     */
    public static void setEndSpawn(MinecraftServer server, GlobalPos pos) {
        if (data == null) load(server);

        data.endSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    /**
     * Retrieves the global end spawn point.
     */
    public static GlobalPos getEndSpawn(MinecraftServer server) {
        if (data == null) load(server);

        if (data != null && data.endSpawn != null) {
            return data.endSpawn.toGlobalPos();
        }
        return null;
    }

    // ==========================================
    // File I/O Operations (Load & Save)
    // ==========================================

    /**
     * Loads the spawn data from the JSON file.
     * If the file does not exist, it initializes a new SpawnData instance.
     */
    private static void load(MinecraftServer server) {
        File file = getFile(server);
        if (!file.exists()) {
            data = new SpawnData();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            data = GSON.fromJson(reader, SpawnData.class);

            if (data == null) {
                data = new SpawnData();
            }
        } catch (IOException e) {
            e.printStackTrace();
            data = new SpawnData();
        }
    }

    /**
     * Saves the current memory state of SpawnData to the JSON file.
     */
    private static void save(MinecraftServer server) {
        // Prevent saving if data has not been initialized or loaded
        if (data == null) return;

        File file = getFile(server);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}