package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.ServerSpawnData;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.manager.PlayerDataManager;
import com.kainax00.simcitymod.manager.TeleportManager;
import com.kainax00.simcitymod.util.PermissionUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
                            info.permissionLevel = newLevel;
                            
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

            // set spawn point command group
            .then(Commands.literal("setspawn")
                .requires(source -> PermissionUtil.hasAdminPermission(source))
                .then(Commands.argument("type", StringArgumentType.word())
                    .suggests((context, builder) -> SharedSuggestionProvider.suggest(new String[]{"wild", "home"}, builder))
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        String type = StringArgumentType.getString(context, "type");
                        GlobalPos currentPos = GlobalPos.of(player.level().dimension(), player.blockPosition());

                        MinecraftServer server = context.getSource().getServer();

                        if (type.equals("wild")) {
                            ServerSpawnData.setWildSpawn(server, currentPos);
                            // Modified to use translation key
                            context.getSource().sendSuccess(() -> 
                                Component.translatable("message.simcitymod.setspawn_wild_success"), true);
                            return Command.SINGLE_SUCCESS;
                        } else if (type.equals("home")) {
                            ServerSpawnData.setHomeSpawn(server, currentPos);
                            // Modified to use translation key
                            context.getSource().sendSuccess(() -> 
                                Component.translatable("message.simcitymod.setspawn_home_success"), true);
                            return Command.SINGLE_SUCCESS;
                        } else {
                            // Modified to use translation key
                            context.getSource().sendFailure(
                                Component.translatable("message.simcitymod.setspawn_invalid"));
                            return 0;
                        }
                    })
                )
            )
        );
    }
}