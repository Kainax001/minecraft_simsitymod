package com.kainax00.simcitymod.registry;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.item.ChunkClaimItem;
import com.kainax00.simcitymod.item.ChunkUnclaimItem;
import com.kainax00.simcitymod.item.ReturnScrollItem;
import com.kainax00.simcitymod.item.TraderSpawnEggItem;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    
    public static final DeferredRegister<Item> ITEMS = 
        DeferredRegister.create(ForgeRegistries.ITEMS, SimcityMod.MOD_ID);

    public static final RegistryObject<Item> CHUNK_CLAIMER = ITEMS.register("chunk_claimer", 
            () -> new ChunkClaimItem(
                new Item.Properties()
                .setId(ITEMS.key("chunk_claimer"))
                .stacksTo(1)
        )
    );

    public static final RegistryObject<Item> CHUNK_UNCLAIMER = ITEMS.register("chunk_unclaimer", 
            () -> new ChunkUnclaimItem(
                new Item.Properties()
                .setId(ITEMS.key("chunk_unclaimer"))
                .stacksTo(1)
        )
    );

    public static final RegistryObject<Item> TRADER_SPAWN_EGG = ITEMS.register("trader_spawn_egg",
            () -> new TraderSpawnEggItem(
                ModEntities.TRADER, 
                0x543618, 
                0x00FF00, 
                new Item.Properties().setId(ITEMS.key("trader_spawn_egg"))
        )
    );

    public static final RegistryObject<Item> RETURN_SCROLL = ITEMS.register("return_scroll",
            () -> new ReturnScrollItem(
                new Item.Properties()
                .setId(ITEMS.key("return_scroll"))
                .stacksTo(16)
        )
    );

    public static void register(BusGroup bus) {
        ITEMS.register(bus);
    }
}