package com.kainax00.simcitymod.registry;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.block.WildernessTeleportBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = 
        DeferredRegister.create(ForgeRegistries.BLOCKS, SimcityMod.MOD_ID);

    public static final RegistryObject<Block> WILDERNESS_TELEPORTER = BLOCKS.register("wilderness_teleporter",
            () -> new WildernessTeleportBlock(
                BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0f, 6.0f)
                .requiresCorrectToolForDrops()
                .setId(BLOCKS.key("wilderness_teleporter"))
            )
    );

    public static void register(BusGroup bus) {
        BLOCKS.register(bus);
    }
}