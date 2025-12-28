package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.manager.ChunkManager;
import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class ChunkCommandExecutor {

    public static int setChunkType(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayerOrException();
            String typeStr = StringArgumentType.getString(context, "type");
            ChunkPos chunkPos = new ChunkPos(player.blockPosition());

            ChunkType type = parseChunkType(typeStr);
            if (type == null) {
                source.sendFailure(Component.translatable("simcitymod.command.chunk.invalid_type"));
                return 0;
            }

            updateChunk(chunkPos, type);
            PlayerDataManager.saveAll(source.getServer());

            source.sendSuccess(() -> Component.translatable("simcitymod.command.chunk.set.success", type.name()), true);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int setChunkSquare(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayerOrException();
            
            int radius = IntegerArgumentType.getInteger(context, "radius");
            String typeStr = StringArgumentType.getString(context, "type");
            
            ChunkType type = parseChunkType(typeStr);
            if (type == null) {
                source.sendFailure(Component.translatable("simcitymod.command.chunk.invalid_type"));
                return 0;
            }

            ChunkPos center = player.chunkPosition();
            int minX = center.x - radius;
            int maxX = center.x + radius;
            int minZ = center.z - radius;
            int maxZ = center.z + radius;

            int count = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    updateChunk(new ChunkPos(x, z), type);
                    count++;
                }
            }

            PlayerDataManager.saveAll(source.getServer());
            
            final int total = count;
            source.sendSuccess(() -> Component.translatable("simcitymod.command.chunk.set.square.success", total, type.name()), true);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int setChunkCircle(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayerOrException();
            
            int radius = IntegerArgumentType.getInteger(context, "radius");
            String typeStr = StringArgumentType.getString(context, "type");
            
            ChunkType type = parseChunkType(typeStr);
            if (type == null) {
                source.sendFailure(Component.translatable("simcitymod.command.chunk.invalid_type"));
                return 0;
            }

            ChunkPos center = player.chunkPosition();
            int count = 0;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dz * dz <= radius * radius) {
                        updateChunk(new ChunkPos(center.x + dx, center.z + dz), type);
                        count++;
                    }
                }
            }

            PlayerDataManager.saveAll(source.getServer());
            
            final int total = count;
            source.sendSuccess(() -> Component.translatable("simcitymod.command.chunk.set.circle.success", total, type.name()), true);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int showChunkInfo(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayerOrException();
            ChunkPos chunkPos = new ChunkPos(player.blockPosition());
            
            ChunkType type = ChunkManager.getChunkType(chunkPos);
            UUID owner = ChunkManager.getOwner(chunkPos);

            String ownerName = (owner != null) ? owner.toString() : Component.translatable("simcitymod.command.chunk.info.owner.none").getString();

            player.displayClientMessage(Component.translatable("simcitymod.command.chunk.info.header"), false);
            player.displayClientMessage(Component.translatable("simcitymod.command.chunk.info.location", chunkPos.toString()), false);
            player.displayClientMessage(Component.translatable("simcitymod.command.chunk.info.type", type.name()), false);
            player.displayClientMessage(Component.translatable("simcitymod.command.chunk.info.owner", ownerName), false);
            
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    private static void updateChunk(ChunkPos pos, ChunkType type) {
        UUID owner = ChunkManager.getOwner(pos);
        if (type != ChunkType.RESIDENTIAL) {
            owner = null;
        }
        ChunkManager.setChunkInfo(pos, owner, type);
    }

    private static ChunkType parseChunkType(String str) {
        try {
            return ChunkType.valueOf(str.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}