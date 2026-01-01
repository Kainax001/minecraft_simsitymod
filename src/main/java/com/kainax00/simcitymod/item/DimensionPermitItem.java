package com.kainax00.simcitymod.item;

import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.manager.PlayerDataManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DimensionPermitItem extends Item {

    private final String dimensionType;

    public DimensionPermitItem(Properties pProperties, String dimensionType) {
        super(pProperties);
        this.dimensionType = dimensionType;
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);

        if (!pLevel.isClientSide()) {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                
                PlayerInfo data = PlayerDataManager.getPlayerData(serverPlayer);
                if (data == null) {
                    return InteractionResult.FAIL;
                }

                boolean hasPermission = dimensionType.equals("nether") ? data.canEnterNether() : data.canEnterEnd();

                if (hasPermission) {
                    pPlayer.displayClientMessage(
                        Component.translatable("item.simcitymod.permit.already_has"), 
                        false
                    );
                    return InteractionResult.FAIL;
                }

                if (dimensionType.equals("nether")) {
                    data.setCanEnterNether(true);
                } else {
                    data.setCanEnterEnd(true);
                }

                PlayerDataManager.saveAll(pLevel.getServer());

                if (!pPlayer.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }

                pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), 
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.0f);

                String msgKey = dimensionType.equals("nether") 
                        ? "item.simcitymod.permit.nether_success" 
                        : "item.simcitymod.permit.end_success";

                pPlayer.displayClientMessage(Component.translatable(msgKey), false);

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.CONSUME;
    }
}