package com.kainax00.simcitymod.config;

import com.kainax00.simcitymod.SimcityMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


/**
 * Configuration class for SimCityMod.
 * Standard Forge config structure without hardcoded messages.
 */
@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // Define only logic-related configs here in the future

    public static ForgeConfigSpec.IntValue MAX_CLAIMS_PER_PLAYER;

    static {
        BUILDER.push("General");
    
        MAX_CLAIMS_PER_PLAYER = BUILDER
            .comment("Maximum number of chunks a player can claim.")
            .defineInRange("maxClaims", 9, 1, 100);
        
        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        // Handle logic when config is loaded
    }
}