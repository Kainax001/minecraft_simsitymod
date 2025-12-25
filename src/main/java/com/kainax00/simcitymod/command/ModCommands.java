package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.PlayerDataManager;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.info.PlayerInfo;
import com.kainax00.simcitymod.util.PermissionUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
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
        dispatcher.register(Commands.literal("simcity")
            .requires(source -> PermissionUtil.hasAdminPermission(source))
            .then(Commands.literal("admin")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("level", IntegerArgumentType.integer(0))
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                            int levelInt = IntegerArgumentType.getInteger(context, "level");
                            
                            PermissionLevel newLevel = PermissionLevel.fromId(levelInt);
                            
                            PlayerInfo info = PlayerDataManager.getPlayerData(target);
                            info.permissionLevel = newLevel;
                            
                            context.getSource().sendSuccess(() -> Component.literal(
                                "Set " + target.getName().getString() + "'s permission level to " + newLevel.name()), true);
                            return 1;
                        })
                    )
                )
            )
        );

        dispatcher.register(Commands.literal("chunk")
            .requires(source -> PermissionUtil.hasAdminPermission(source))
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
        );
    }
}