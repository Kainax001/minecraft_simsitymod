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

public class ServerSpawnData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "simcity_server_spawn.json";

    /**
     * Inner class to represent the spawn data structure.
     * Stores information for both wild and home spawn points.
     */
    private static class SpawnData {
        HomeInfo wildSpawn;
        HomeInfo homeSpawn;
    }

    private static SpawnData data = new SpawnData();

    private static File getFile(MinecraftServer server) {
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        return worldPath.resolve(FILE_NAME).toFile();
    }

    // ==========================================
    // 1. Wild Spawn Methods
    // ==========================================
    public static void setWildSpawn(MinecraftServer server, GlobalPos pos) {
        if (data == null) load(server);
        
        data.wildSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

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
    public static void setHomeSpawn(MinecraftServer server, GlobalPos pos) {
        if (data == null) load(server);
        
        data.homeSpawn = HomeInfo.fromGlobalPos(pos);
        save(server);
    }

    public static GlobalPos getHomeSpawn(MinecraftServer server) {
        if (data == null) load(server);

        if (data != null && data.homeSpawn != null) {
            return data.homeSpawn.toGlobalPos();
        }
        return null; 
    }

    // ==========================================
    // File I/O Operations (Load & Save)
    // ==========================================
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

    private static void save(MinecraftServer server) {
        File file = getFile(server);
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}