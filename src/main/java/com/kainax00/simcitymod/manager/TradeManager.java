package com.kainax00.simcitymod.manager;

import com.kainax00.simcitymod.config.TradeConfig;
import com.kainax00.simcitymod.config.TradeConfig.TradeRecipe;
import com.kainax00.simcitymod.util.IdentifierUtil;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class TradeManager {

    public static TradeRecipe getMatchingTrade(ItemStack heldItem) {
        if (heldItem.isEmpty()) return null;

        for (TradeRecipe recipe : TradeConfig.getTrades()) {
            Identifier id = IdentifierUtil.parse(recipe.inputItem);
            
            Item requiredItem = ForgeRegistries.ITEMS.getValue(id);
            
            if (requiredItem != null && heldItem.getItem() == requiredItem && heldItem.getCount() >= recipe.inputCount) {
                return recipe;
            }
        }
        return null;
    }
    
    public static ItemStack getResultItem(TradeRecipe recipe) {
        Identifier id = IdentifierUtil.parse(recipe.outputItem);
        
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) return ItemStack.EMPTY;
        
        return new ItemStack(item, recipe.outputCount);
    }
}