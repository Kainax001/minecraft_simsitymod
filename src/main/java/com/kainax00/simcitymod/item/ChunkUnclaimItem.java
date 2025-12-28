package com.kainax00.simcitymod.item;

import com.kainax00.simcitymod.manager.ChunkManager;
import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.config.Config;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level; 

import java.util.UUID;

public class ChunkUnclaimItem extends Item {
    public ChunkUnclaimItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {

        if (!pContext.getLevel().isClientSide() && pContext.getLevel() instanceof ServerLevel serverLevel) {
            Player player = pContext.getPlayer();
            if (player == null) return InteractionResult.FAIL;

            if (serverLevel.dimension() != Level.OVERWORLD) {
                player.displayClientMessage(
                    Component.literal("이 차원에서는 점유를 조작할 수 없습니다."), 
                    false
                );
                return InteractionResult.FAIL;
            }

            ChunkPos chunkPos = new ChunkPos(pContext.getClickedPos());
            UUID currentOwner = ChunkManager.getOwner(chunkPos);

            if (currentOwner == null) {
                player.displayClientMessage(
                    Component.translatable("message.simcitymod.chunk_not_claimed"), 
                    false
                );
                return InteractionResult.PASS;
            }

            if (currentOwner.equals(player.getUUID())) {
                PlayerInfo data = PlayerDataManager.getOrCreateData(player.getUUID(), player.getName().getString());

                int currentBase = Config.MAX_CLAIMS_PER_PLAYER.get();
                data.setBaseLimit(currentBase);
                data.setMaxLimit(currentBase + data.getBonusLimit());

                ChunkManager.setChunkInfo(chunkPos, null, ChunkType.RESIDENTIAL);

                if (data.getClaimedChunks().remove(chunkPos.toLong())) {
                    data.setClaimedCount(data.getClaimedCount() - 1);
                }

                PlayerDataManager.saveAll(serverLevel.getServer());

                if (!player.getAbilities().instabuild) {
                    pContext.getItemInHand().shrink(1);
                }

                player.displayClientMessage(
                    Component.translatable("message.simcitymod.chunk_unclaimed", 
                        data.getClaimedCount(), 
                        data.getMaxLimit()), 
                    false
                );
                
            } else {
                player.displayClientMessage(
                    Component.translatable("message.simcitymod.not_your_chunk"), 
                    false
                );
            }
        }
        return InteractionResult.SUCCESS;
    }
}