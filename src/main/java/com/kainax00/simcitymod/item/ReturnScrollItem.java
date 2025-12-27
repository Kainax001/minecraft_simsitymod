package com.kainax00.simcitymod.item;

import com.kainax00.simcitymod.manager.TeleportManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ReturnScrollItem extends Item {

    public ReturnScrollItem(Item.Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            
            boolean success = TeleportManager.teleportToHome(serverPlayer);

            if (success) {
                player.getCooldowns().addCooldown(itemStack, 20);

                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                return InteractionResult.CONSUME.heldItemTransformedTo(itemStack);
            }
        }

        return InteractionResult.PASS;
    }
}