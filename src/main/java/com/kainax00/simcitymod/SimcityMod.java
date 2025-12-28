package com.kainax00.simcitymod;

import com.kainax00.simcitymod.client.renderer.TraderRenderer;
import com.kainax00.simcitymod.config.Config;
import com.kainax00.simcitymod.config.TradeConfig;
import com.kainax00.simcitymod.event.ClaimProtectionHandler;
import com.kainax00.simcitymod.registry.ModItems;
import com.kainax00.simcitymod.registry.ModEntities;

import com.mojang.logging.LogUtils;

import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SimcityMod.MOD_ID)
public final class SimcityMod {
    //define mod id in a common place for everything to reference
    public static final String MOD_ID = "simcitymod";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimcityMod(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();

        ModItems.ITEMS.register(modBusGroup);
        ModEntities.ENTITIES.register(modBusGroup);

        // Register the commonSetup method for modloading
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::commonSetup);
        FMLClientSetupEvent.getBus(modBusGroup).addListener(this::clientSetup);

        // Register the item to a creative tab
        BuildCreativeModeTabContentsEvent.BUS.addListener(SimcityMod::addCreative);
        
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        
        // chunk protection event
        MinecraftForge.EVENT_BUS.register(ClaimProtectionHandler.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            TradeConfig.load(); 
        });
    }

    // Add the example block item to the building blocks tab
    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.CHUNK_CLAIMER.get()); 
            event.accept(ModItems.CHUNK_UNCLAIMER.get());
            event.accept(ModItems.TRADER_SPAWN_EGG.get());
            event.accept(ModItems.RETURN_SCROLL.get());
            event.accept(ModItems.CHUNK_LIMIT_INCREASE_ITEM.get());
        }
    }

    // Register entity renderers during the client setup phase
    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EntityRenderers.register(ModEntities.TRADER.get(), TraderRenderer::new);
        });
    }
}
