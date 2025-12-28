package com.kainax00.simcitymod.item;

import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ChunkLimitIncreaseItem extends Item {

    public ChunkLimitIncreaseItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide()) {
            PlayerInfo data = PlayerDataManager.getOrCreateData(pPlayer.getUUID(), pPlayer.getName().getString());

            data.maxLimit += 1;

            PlayerDataManager.saveAll(pLevel.getServer());

            if (!pPlayer.getAbilities().instabuild) {
                itemStack.shrink(1);
            }

            pPlayer.displayClientMessage(
                Component.translatable("message.simcitymod.limit_increase_success", data.maxLimit),
                false
            );

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }
}