package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.ServerSpawnData;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.manager.ChunkPreGenerator;
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
import net.minecraft.server.level.ServerLevel;
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

        // Root command: /sim
        dispatcher.register(Commands.literal("sim")

            // -----------------------------------------------------
            // 1. ADMIN GROUP: All commands under here require admin
            // Structure: /sim admin [tp|chunk|setspawn|maintenance|<player>...]
            // -----------------------------------------------------
            .then(Commands.literal("admin")
                .requires(source -> PermissionUtil.hasAdminPermission(source))

                // 1-1. Permission Management: /sim admin <target> <level>
                // (Arguments sit alongside literals; valid in Brigadier)
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("level", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                            int levelInt = IntegerArgumentType.getInteger(context, "level");
                            
                            PermissionLevel newLevel = PermissionLevel.fromId(levelInt);
                            
                            PlayerInfo info = PlayerDataManager.getPlayerData(target);

                            info.setPermissionLevel(newLevel);
                            PlayerDataManager.saveAll(context.getSource().getServer());
                            
                            context.getSource().sendSuccess(() -> Component.translatable(
                                "message.simcitymod.admin_set_success", 
                                target.getName().getString(), 
                                newLevel.name()), true);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )

                // 1-2. Teleport Group: /sim admin tp <wild|home>
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

                // 1-3. Chunk Management: /sim admin chunk ...
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

                // 1-4. Set Spawn: /sim admin setspawn ...
                .then(Commands.literal("setspawn")
                    .then(Commands.argument("type", StringArgumentType.word())
                        // Added "nether", "end" to suggestions
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"wild", "home", "nether", "end"}, builder))
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
                                
                            } else if (type.equals("nether")) {
                                if (currentDimension != Level.NETHER) {
                                    context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_nether_nether_only"));
                                    return 0;
                                }

                                ServerSpawnData.setNetherSpawn(server, currentPos);
                                context.getSource().sendSuccess(() -> 
                                    Component.translatable("message.simcitymod.setspawn_nether_success"), true);
                                return Command.SINGLE_SUCCESS;

                            } else if (type.equals("end")) {
                                if (currentDimension != Level.END) {
                                    context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_end_end_only"));
                                    return 0;
                                }

                                ServerSpawnData.setEndSpawn(server, currentPos);
                                context.getSource().sendSuccess(() -> 
                                    Component.translatable("message.simcitymod.setspawn_end_success"), true);
                                return Command.SINGLE_SUCCESS;

                            } else {
                                context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_invalid"));
                                return 0;
                            }
                        })
                    )
                )

                // 1-5. Maintenance: /sim admin maintenance ...
                .then(Commands.literal("maintenance")
                    .then(Commands.literal("reset")
                        .then(Commands.argument("type", StringArgumentType.word())
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
                    .then(Commands.literal("pregen")
                        .executes(context -> {
                            ServerLevel level = context.getSource().getLevel();
                            ChunkPreGenerator.startPreGeneration(level);
                            context.getSource().sendSuccess(() -> 
                            Component.translatable("message.simcitymod.pregen_start"), 
                            true);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(Commands.literal("stop")
                            .executes(context -> {
                                ChunkPreGenerator.stopPreGeneration();
                                context.getSource().sendSuccess(() -> 
                                Component.translatable("message.simcitymod.pregen_stop_requested"), 
                                true);
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                )

                // 1-6. Set Center: /sim admin setcenter
                .then(Commands.literal("setcenter")
                    .executes(WorldCommandExecutor::setWorldCenter)
                )
            )

            // -----------------------------------------------------
            // 2. PUBLIC GROUP: Commands accessible by everyone
            // -----------------------------------------------------

            // 2-1. Friend Management: /sim friend ...
            .then(Commands.literal("friend")
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