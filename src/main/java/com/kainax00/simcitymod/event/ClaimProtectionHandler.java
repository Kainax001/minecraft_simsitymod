package com.kainax00.simcitymod.event;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.ChunkManager;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.registry.ModItems;
import com.kainax00.simcitymod.util.PermissionUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.util.Result;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID)
public class ClaimProtectionHandler {
    private static final WeakHashMap<UUID, Long> messageCooldowns = new WeakHashMap<>();
    private static final long COOLDOWN_MS = 500;

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        if (isProtected(event.getPlayer(), event.getPos(), event.getLevel())) {
            event.setResult(Result.DENY);

            long currentTime = System.currentTimeMillis();
            long lastTime = messageCooldowns.getOrDefault(player.getUUID(), 0L);

            if (currentTime - lastTime > COOLDOWN_MS) {
                sendDenyMessage(event.getPlayer());
                messageCooldowns.put(player.getUUID(), currentTime);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        
        Player player = event.getEntity();

        if (player.getItemInHand(event.getHand()).is(ModItems.CHUNK_UNCLAIMER.get())) return;

        if (isProtected(player, event.getPos(), event.getLevel())) {
            event.setUseBlock(Result.DENY);
            event.setUseItem(Result.DENY);

            long currentTime = System.currentTimeMillis();
            long lastTime = messageCooldowns.getOrDefault(player.getUUID(), 0L);

            if (currentTime - lastTime > COOLDOWN_MS) {
                sendDenyMessage(event.getEntity());
                messageCooldowns.put(player.getUUID(), currentTime);
            }
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> {
            UUID owner = ChunkManager.getOwner(new ChunkPos(pos));
            return owner != null;
        });
    }

    private static void sendDenyMessage(Player player) {
        player.displayClientMessage(Component.translatable("message.simcitymod.chunk_protected"), false);
    }

    private static boolean isProtected(Player player, BlockPos pos, LevelAccessor level) {
        if (player == null) return false;

        if (PermissionUtil.hasAdminPermission(player)) {
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(pos);
        ChunkType type = ChunkManager.getChunkType(chunkPos);

        if (type == ChunkType.RESIDENTIAL) {
            UUID owner = ChunkManager.getOwner(chunkPos);
            return owner != null && !owner.equals(player.getUUID());
        }

        return false;
    }
}