package com.kainax00.simcitymod.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.MaintenanceData;
import com.kainax00.simcitymod.data.enums.SimDimensionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class DynamicDimensionManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static boolean scheduleNextDimension(MinecraftServer server, SimDimensionType type, Long customSeed) {
        try {
            int nextVersion = MaintenanceData.incrementVersion(server, type);
            String dimName = type.getIdPrefix() + "_v" + nextVersion;

            Path datapackDir = server.getWorldPath(LevelResource.DATAPACK_DIR).resolve("simcity_dynamic_dims");
            Path dimensionDir = datapackDir.resolve("data").resolve("simcitymod").resolve("dimension");
            if (!Files.exists(dimensionDir)) Files.createDirectories(dimensionDir);

            archiveWorldFolder(server, dimName);

            File metaFile = datapackDir.resolve("pack.mcmeta").toFile();
            if (!metaFile.exists()) createPackMeta(metaFile);

            long seed = (customSeed != null) ? customSeed : new Random().nextLong();

            createDimensionJson(dimensionDir.resolve(dimName + ".json").toFile(), type, seed);

            SimcityMod.LOGGER.info("Scheduled NEW dimension: " + dimName);
            return true;
        } catch (Exception e) {
            SimcityMod.LOGGER.error("Failed to schedule dimension reset", e);
            return false;
        }
    }

    private static void archiveWorldFolder(MinecraftServer server, String dimName) {
        Path worldPath = server.getWorldPath(LevelResource.ROOT).resolve("dimensions/simcitymod").resolve(dimName);
        if (Files.exists(worldPath)) {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
                Path archivePath = server.getWorldPath(LevelResource.ROOT).resolve("dimensions/simcitymod").resolve(dimName + "_old_" + timestamp);
                Files.move(worldPath, archivePath, StandardCopyOption.REPLACE_EXISTING);
                SimcityMod.LOGGER.info("Archived old world data: " + dimName);
            } catch (IOException e) {
                SimcityMod.LOGGER.warn("Failed to archive world folder: " + dimName);
            }
        }
    }

    private static void createPackMeta(File file) throws IOException {
        JsonObject pack = new JsonObject();
        JsonObject meta = new JsonObject();
        meta.addProperty("pack_format", 48); 
        meta.addProperty("description", "SimCity Dimensions");
        pack.add("pack", meta);
        try (FileWriter writer = new FileWriter(file)) { GSON.toJson(pack, writer); }
    }

    private static void createDimensionJson(File file, SimDimensionType type, long seed) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("type", type.getDimensionType());

        JsonObject generator = new JsonObject();
        generator.addProperty("type", "minecraft:noise");
        generator.addProperty("seed", seed);
        generator.addProperty("settings", type.getGeneratorSettings()); 

        JsonObject biomeSource = new JsonObject();

        if (type == SimDimensionType.END) {
            biomeSource.addProperty("type", "minecraft:the_end");
            biomeSource.addProperty("seed", seed); 
            
        } else {
            biomeSource.addProperty("type", "minecraft:multi_noise");
            biomeSource.addProperty("preset", type.getGeneratorSettings());
        }

        generator.add("biome_source", biomeSource);
        root.add("generator", generator);

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
        }
    }
}