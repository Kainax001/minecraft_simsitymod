package com.kainax00.simcitymod.event;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.entity.TraderEntity;
import com.kainax00.simcitymod.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TRADER.get(), TraderEntity.createAttributes().build());
    }
}