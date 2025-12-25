package com.kainax00.simcitymod.registry;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.PlayerDataManager;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Load both individual player data and global claims
        PlayerDataManager.loadAll(event.getServer());
        SimcityMod.LOGGER.info(">>> [SimCityMod] All data (Players & Claims) loaded successfully.");
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // Save all data when the server stops
        PlayerDataManager.saveAll(event.getServer());
        SimcityMod.LOGGER.info(">>> [SimCityMod] All data (Players & Claims) saved successfully.");
    }
    
    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        // Periodic save during level save events to prevent data loss
        if (event.getLevel().getServer() != null) {
            PlayerDataManager.saveAll(event.getLevel().getServer());
        }
    }
}