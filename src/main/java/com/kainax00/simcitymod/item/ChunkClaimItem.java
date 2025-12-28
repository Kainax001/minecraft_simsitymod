package com.kainax00.simcitymod.item;

import java.util.UUID;
import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.manager.ChunkManager;
import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.config.Config;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class ChunkClaimItem extends Item {
    public ChunkClaimItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide() && pContext.getLevel() instanceof ServerLevel serverLevel) {
            Player player = pContext.getPlayer();
            if (player == null) return InteractionResult.FAIL;

            if (serverLevel.dimension() != Level.OVERWORLD) {
                player.displayClientMessage(
                    Component.translatable("message.simcitymod.chunk_not_residential"), 
                    false
                );
                return InteractionResult.FAIL;
            }

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

                int currentBase = Config.MAX_CLAIMS_PER_PLAYER.get();
                data.setBaseLimit(currentBase);
                data.setMaxLimit(currentBase + data.getBonusLimit());

                int currentCount = data.getClaimedCount();
                int maxAllowed = data.getMaxLimit();
                
                if (currentCount >= maxAllowed) {
                    player.displayClientMessage(
                        Component.translatable("message.simcitymod.claim_limit_exceeded", currentCount, maxAllowed), 
                        false
                    );
                    return InteractionResult.FAIL;
                }

                ChunkManager.setOwner(chunkPos, player.getUUID());
                data.getClaimedChunks().add(chunkPos.toLong());
                data.setClaimedCount(data.getClaimedCount() + 1);

                PlayerDataManager.saveAll(serverLevel.getServer());

                if (!player.getAbilities().instabuild) {
                    pContext.getItemInHand().shrink(1);
                }
                
                SimcityMod.LOGGER.info(">>> [SimCityMod] Chunk claimed by " + player.getName().getString() + ": " + chunkPos);

                player.displayClientMessage(
                    Component.translatable("message.simcitymod.chunk_claimed_success", data.getClaimedCount(), maxAllowed),
                    false);

            } else {
                if (currentOwner.equals(player.getUUID())) {
                    player.displayClientMessage(Component.translatable("message.simcitymod.chunk_already_owned"), false);
                } else {
                    String ownerName = PlayerDataManager.getPlayerName(currentOwner);
                    player.displayClientMessage(Component.translatable("message.simcitymod.chunk_already_claimed", ownerName), false);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}