package com.kainax00.simcitymod.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kainax00.simcitymod.data.enums.SimDimensionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages dimension versioning, starting from v0 for the initial world.
 */
public class MaintenanceData {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "simcity_dimension_versions.json";

    private static class Data {
        Map<String, Integer> versions = new HashMap<>();
    }

    private static Data data = new Data();

    private static File getFile(MinecraftServer server) {
        Path worldPath = server.getWorldPath(LevelResource.ROOT);
        return worldPath.resolve(FILE_NAME).toFile();
    }

    /**
     * Returns the current version. Defaults to 0 (initial version).
     */
    public static int getCurrentVersion(MinecraftServer server, SimDimensionType type) {
        load(server);
        // Default changed from 1 to 0
        return data.versions.getOrDefault(type.name(), 0);
    }

    /**
     * Increments version (0 -> 1 -> 2...).
     */
    public static int incrementVersion(MinecraftServer server, SimDimensionType type) {
        load(server);
        int current = getCurrentVersion(server, type);
        int next = current + 1;
        data.versions.put(type.name(), next);
        save(server);
        return next;
    }

    private static void load(MinecraftServer server) {
        File file = getFile(server);
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            data = GSON.fromJson(reader, Data.class);
            if (data == null) data = new Data();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private static void save(MinecraftServer server) {
        try (FileWriter writer = new FileWriter(getFile(server))) {
            GSON.toJson(data, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }
}