package com.kainax00.simcitymod.config;

import com.kainax00.simcitymod.SimcityMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // == General ==
    public static ForgeConfigSpec.IntValue MAX_CLAIMS_PER_PLAYER;

    // == Dimension Settings ==
    public static ForgeConfigSpec.DoubleValue WILD_BORDER_SIZE;
    public static ForgeConfigSpec.DoubleValue RESIDENTIAL_BORDER_SIZE;
    public static ForgeConfigSpec.DoubleValue NETHER_BORDER_SIZE;
    public static ForgeConfigSpec.DoubleValue END_BORDER_SIZE;    
    
    public static ForgeConfigSpec.DoubleValue BORDER_DAMAGE;
    public static ForgeConfigSpec.IntValue BORDER_WARNING;

    static {
        BUILDER.push("General");
        MAX_CLAIMS_PER_PLAYER = BUILDER
            .comment("Maximum number of chunks a player can claim.")
            .defineInRange("maxClaims", 4, 1, 16);
        BUILDER.pop();

        BUILDER.push("Dimension Settings");

        WILD_BORDER_SIZE = BUILDER
                .comment("Border diameter for the Wild Dimension.")
                .defineInRange("wildBorderSize", 5000.0, 100.0, 30000000.0);

        RESIDENTIAL_BORDER_SIZE = BUILDER
                .comment("Border diameter for the Residential (Overworld).")
                .defineInRange("residentialBorderSize", 1000.0, 100.0, 30000000.0);

        NETHER_BORDER_SIZE = BUILDER
                .comment("Border diameter for the Nether.")
                .defineInRange("netherBorderSize", 4000.0, 100.0, 30000000.0);

        END_BORDER_SIZE = BUILDER
                .comment("Border diameter for the End.")
                .defineInRange("endBorderSize", 6000.0, 100.0, 30000000.0);

        BORDER_DAMAGE = BUILDER
                .comment("Damage per second outside the border.")
                .defineInRange("borderDamage", 5.0, 0.0, 1000.0);

        BORDER_WARNING = BUILDER
                .comment("Warning distance (blocks) before the border.")
                .defineInRange("borderWarning", 50, 0, 1000);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}