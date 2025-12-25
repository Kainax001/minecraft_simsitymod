package com.kainax00.simcitymod.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import java.util.function.Supplier;

public class TraderSpawnEggItem extends SpawnEggItem {
    private final Supplier<? extends EntityType<? extends Mob>> typeSupplier;
    
    public TraderSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Item.Properties properties) {
        super(properties); 
        this.typeSupplier = type;
    }

    @Override
    public EntityType<?> getType(ItemStack pStack) {
        return typeSupplier.get();
    }
}