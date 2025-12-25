package com.kainax00.simcitymod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kainax00.simcitymod.SimcityMod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static List<TradeRecipe> loadedTrades = new ArrayList<>();

    private static final String INTERNAL_PATH = "/assets/simcitymod/config/trades.json";

    public static class TradeRecipe {
        public String inputItem;
        public int inputCount;
        public String outputItem;
        public int outputCount;
    }

    public static void load() {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("simcitymod");
        File configFile = configDir.resolve("trades.json").toFile();

        try {
            if (!configDir.toFile().exists()) {
                configDir.toFile().mkdirs();
            }

            if (!configFile.exists()) {
                copyDefaultFromResources(configFile);
            }

            try (FileReader reader = new FileReader(configFile)) {
                loadedTrades = GSON.fromJson(reader, new TypeToken<List<TradeRecipe>>(){}.getType());
                SimcityMod.LOGGER.info("SimCityMod: Trade config loaded. (Count: " + (loadedTrades != null ? loadedTrades.size() : 0) + ")");
            }

        } catch (Exception e) {
            SimcityMod.LOGGER.error("Error loading trade config", e);
            loadedTrades = new ArrayList<>();
        }
    }

    private static void copyDefaultFromResources(File destination) {
        try (InputStream is = TradeConfig.class.getResourceAsStream(INTERNAL_PATH)) {
            if (is == null) {
                SimcityMod.LOGGER.error("CRITICAL: Internal resource file not found: " + INTERNAL_PATH);
                return;
            }

            Files.copy(is, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            SimcityMod.LOGGER.info("SimCityMod: Default config file copied from resources.");

        } catch (IOException e) {
            SimcityMod.LOGGER.error("Failed to create default config file", e);
        }
    }

    public static List<TradeRecipe> getTrades() {
        if (loadedTrades == null) return new ArrayList<>();
        return Collections.unmodifiableList(loadedTrades);
    }
}