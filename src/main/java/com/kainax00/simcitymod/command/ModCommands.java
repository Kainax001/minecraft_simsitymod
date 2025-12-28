package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.ServerSpawnData;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.manager.MaintenanceManager;
import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.manager.TeleportManager;
import com.kainax00.simcitymod.util.PermissionUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.Level;


import java.util.Arrays;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // 1. /sim command group (Admin only)
        dispatcher.register(Commands.literal("sim")
            .requires(source -> PermissionUtil.hasAdminPermission(source))

            // permission management command
            .then(Commands.literal("admin")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("level", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                            int levelInt = IntegerArgumentType.getInteger(context, "level");
                            
                            PermissionLevel newLevel = PermissionLevel.fromId(levelInt);
                            
                            PlayerInfo info = PlayerDataManager.getPlayerData(target);

                            info.setPermissionLevel(newLevel);
                            PlayerDataManager.saveAll(context.getSource().getServer());
                            
                            // Modified to use translation key
                            context.getSource().sendSuccess(() -> Component.translatable(
                                "message.simcitymod.admin_set_success", 
                                target.getName().getString(), 
                                newLevel.name()), true);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )

            // transport command group
            .then(Commands.literal("tp")
                .then(Commands.literal("wild")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        if (TeleportManager.teleportToWild(player)) {
                            return Command.SINGLE_SUCCESS;
                        }
                        return 0;
                    })
                )
                .then(Commands.literal("home")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        if (TeleportManager.teleportToHome(player)) {
                            return Command.SINGLE_SUCCESS;
                        }
                        return 0;
                    })
                )
            )

            // chunk command group
            .then(Commands.literal("chunk")
                .then(Commands.literal("settype")
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                            Arrays.stream(ChunkType.values()).map(Enum::name).collect(Collectors.toList()), 
                            builder
                        ))
                        .executes(ChunkCommandExecutor::setChunkType)
                    )
                )
                .then(Commands.literal("setsquare")
                    .then(Commands.argument("radius", IntegerArgumentType.integer(0, 32))
                        .then(Commands.argument("type", StringArgumentType.word())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Arrays.stream(ChunkType.values()).map(Enum::name).collect(Collectors.toList()), 
                                builder
                            ))
                            .executes(ChunkCommandExecutor::setChunkSquare)
                        )
                    )
                )
                .then(Commands.literal("setcircle")
                    .then(Commands.argument("radius", IntegerArgumentType.integer(0, 32))
                        .then(Commands.argument("type", StringArgumentType.word())
                            .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Arrays.stream(ChunkType.values()).map(Enum::name).collect(Collectors.toList()), 
                                builder
                            ))
                            .executes(ChunkCommandExecutor::setChunkCircle)
                        )
                    )
                )
                .then(Commands.literal("info")
                    .executes(ChunkCommandExecutor::showChunkInfo)
                )
            )

            /// set spawn point command group
            .then(Commands.literal("setspawn")
                .then(Commands.argument("type", StringArgumentType.word())
                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"wild", "home"}, builder))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        String type = StringArgumentType.getString(context, "type");

                        ResourceKey<Level> currentDimension = player.level().dimension();
                        GlobalPos currentPos = GlobalPos.of(currentDimension, player.blockPosition());
                        MinecraftServer server = context.getSource().getServer();

                        if (type.equals("wild")) {
                            if (currentDimension != Level.OVERWORLD) {
                                context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_wild_overworld_only"));
                                return 0;
                            }
                            
                            ServerSpawnData.setWildSpawn(server, currentPos);
                            context.getSource().sendSuccess(() -> 
                                Component.translatable("message.simcitymod.setspawn_wild_success"), true);
                            return Command.SINGLE_SUCCESS;
                            
                        } else if (type.equals("home")) {
                            if (currentDimension != Level.OVERWORLD) {
                                context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_home_non_overworld_only"));
                                return 0;
                            }
                            
                            ServerSpawnData.setHomeSpawn(server, currentPos);
                            context.getSource().sendSuccess(() -> 
                                Component.translatable("message.simcitymod.setspawn_home_success"), true);
                            return Command.SINGLE_SUCCESS;
                            
                        } else {
                            context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_invalid"));
                            return 0;
                        }
                    })
                )
            )

            // Maintenance command group (Terrain Reset)
            .then(Commands.literal("maintenance")
                .then(Commands.literal("reset")
                    .then(Commands.argument("type", StringArgumentType.word())
                        // Suggesting types: wild, nether, end
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(new String[]{"wild", "nether", "end"}, builder))
                        .executes(context -> MaintenanceManager.executeReset(
                            context, StringArgumentType.getString(context, "type"), null))
                        .then(Commands.argument("seed", LongArgumentType.longArg())
                            .executes(context -> MaintenanceManager.executeReset(
                                context, 
                                StringArgumentType.getString(context, "type"), 
                                LongArgumentType.getLong(context, "seed")))
                        )
                    )
                )
            )

            .then(Commands.literal("setcenter")
                .executes(WorldCommandExecutor::setWorldCenter)
            )
            
            // Add new command here
        );

        // /sc friend command group (All players)
        dispatcher.register(Commands.literal("sc")
            .then(Commands.literal("friend")
                // use: /sc friend add <player>
                .then(Commands.literal("add")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer owner = context.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");

                            if (owner.getUUID().equals(target.getUUID())) {
                                context.getSource().sendFailure(Component.translatable("message.simcitymod.friend_add_self"));
                                return 0;
                            }

                            boolean success = PlayerDataManager.addFriend(owner.getUUID(), target.getUUID(), context.getSource().getServer());

                            if (success) {
                                context.getSource().sendSuccess(() -> Component.translatable(
                                    "message.simcitymod.friend_add_success", target.getName().getString()), true);
                            } else {
                                context.getSource().sendFailure(Component.translatable("message.simcitymod.friend_already_exists"));
                            }
                            return success ? Command.SINGLE_SUCCESS : 0;
                        })
                    )
                )
                
                // use: /sc friend remove <player>
                .then(Commands.literal("remove")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer owner = context.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");

                            boolean success = PlayerDataManager.removeFriend(owner.getUUID(), target.getUUID(), context.getSource().getServer());

                            if (success) {
                                context.getSource().sendSuccess(() -> Component.translatable(
                                    "message.simcitymod.friend_remove_success", target.getName().getString()), true);
                            } else {
                                context.getSource().sendFailure(Component.translatable("message.simcitymod.friend_not_found"));
                            }
                            return success ? Command.SINGLE_SUCCESS : 0;
                        })
                    )
                )
            )
        );


    }
}