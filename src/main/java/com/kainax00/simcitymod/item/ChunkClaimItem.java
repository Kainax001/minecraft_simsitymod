package com.kainax00.simcitymod.item;

import java.util.UUID;
import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.manager.ChunkManager;
import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.info.PlayerInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;

public class ChunkClaimItem extends Item {
    public ChunkClaimItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide() && pContext.getLevel() instanceof ServerLevel serverLevel) {
            Player player = pContext.getPlayer();
            if (player == null) return InteractionResult.FAIL;

            ChunkPos chunkPos = new ChunkPos(pContext.getClickedPos());

            ChunkType type = ChunkManager.getChunkType(chunkPos);
            if (type != ChunkType.RESIDENTIAL) {
                player.displayClientMessage(
                    Component.translatable("message.simcitymod.chunk_not_residential"), 
                    false
                );
                return InteractionResult.FAIL;
            }

            UUID currentOwner = ChunkManager.getOwner(chunkPos);

            if (currentOwner == null) {
                PlayerInfo data = PlayerDataManager.getOrCreateData(player.getUUID(), player.getName().getString());

                int currentCount = data.claimedCount;
                int maxAllowed = data.maxLimit;
                
                if (currentCount >= maxAllowed) {
                    player.displayClientMessage(
                        Component.translatable("message.simcitymod.claim_limit_exceeded", currentCount, maxAllowed), 
                        false
                    );
                    return InteractionResult.FAIL;
                }

                ChunkManager.setOwner(chunkPos, player.getUUID());
                data.claimedChunks.add(chunkPos.toLong());
                data.claimedCount++;

                PlayerDataManager.saveAll(serverLevel.getServer());

                if (!player.getAbilities().instabuild) {
                    pContext.getItemInHand().shrink(1);
                }
                
                SimcityMod.LOGGER.info(">>> [SimCityMod] Chunk claimed by " + player.getName().getString() + ": " + chunkPos);

                player.displayClientMessage(
                    Component.translatable("message.simcitymod.chunk_claimed_success", data.claimedCount, maxAllowed),
                    false);

            } else {
                if (currentOwner.equals(player.getUUID())) {
                    player.displayClientMessage(Component.translatable("message.simcitymod.chunk_already_owned"), false);
                } else {
                    player.displayClientMessage(Component.translatable("message.simcitymod.chunk_already_claimed", "Someone"), false);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}