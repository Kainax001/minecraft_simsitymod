package com.kainax00.simcitymod.registry;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.entity.TraderEntity;
import com.kainax00.simcitymod.util.IdentifierUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SimcityMod.MOD_ID);

    public static final RegistryObject<EntityType<TraderEntity>> TRADER = ENTITIES.register("trader",
            () -> EntityType.Builder.of(TraderEntity::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .build(ResourceKey.create(
                            Registries.ENTITY_TYPE,
                            IdentifierUtil.create(SimcityMod.MOD_ID, "trader")
                    ))
    );

    public static void register(BusGroup bus) {
        ENTITIES.register(bus);
    }
}