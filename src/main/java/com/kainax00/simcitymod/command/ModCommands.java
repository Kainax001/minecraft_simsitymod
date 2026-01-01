package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.data.ServerSpawnData;
import com.kainax00.simcitymod.data.enums.ChunkType;
import com.kainax00.simcitymod.data.enums.PermissionLevel;
import com.kainax00.simcitymod.data.enums.SimDimensionType;
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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sim")
            // -----------------------------------------------------
            // 1. ADMIN GROUP
            // -----------------------------------------------------
            .then(Commands.literal("admin")
                .requires(source -> PermissionUtil.hasAdminPermission(source))

                // 1-1. Permission
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("level", IntegerArgumentType.integer(0))
                        .executes(ModCommands::setPermissionLevel)
                    )
                )

                // 1-2. Teleport
                .then(Commands.literal("tp")
                    .then(Commands.literal("wild").executes(ctx -> executeTeleport(ctx, TeleportManager::teleportToWild)))
                    .then(Commands.literal("home").executes(ctx -> executeTeleport(ctx, TeleportManager::teleportToHome)))
                    .then(Commands.literal("nether").executes(ctx -> executeTeleport(ctx, TeleportManager::teleportToNether)))
                    .then(Commands.literal("end").executes(ctx -> executeTeleport(ctx, TeleportManager::teleportToEnd)))
                )

                // 1-3. Chunk
                .then(Commands.literal("chunk")
                    .then(Commands.literal("settype")
                        .then(Commands.argument("type", StringArgumentType.word())
                            .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Arrays.stream(ChunkType.values()).map(Enum::name).collect(Collectors.toList()), b))
                            .executes(ChunkCommandExecutor::setChunkType)
                        )
                    )
                    .then(Commands.literal("setsquare")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, 32))
                            .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Arrays.stream(ChunkType.values()).map(Enum::name).collect(Collectors.toList()), b))
                                .executes(ChunkCommandExecutor::setChunkSquare)
                            )
                        )
                    )
                    .then(Commands.literal("setcircle")
                        .then(Commands.argument("radius", IntegerArgumentType.integer(0, 32))
                            .then(Commands.argument("type", StringArgumentType.word())
                                .suggests((ctx, b) -> SharedSuggestionProvider.suggest(Arrays.stream(ChunkType.values()).map(Enum::name).collect(Collectors.toList()), b))
                                .executes(ChunkCommandExecutor::setChunkCircle)
                            )
                        )
                    )
                    .then(Commands.literal("info").executes(ChunkCommandExecutor::showChunkInfo))
                )

                // 1-4. Set Spawn
                .then(Commands.literal("setspawn")
                    .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"wild", "home", "nether", "end"}, b))
                        .executes(ModCommands::executeSetSpawn)
                    )
                )

                // 1-5. Maintenance
                .then(Commands.literal("maintenance")
                    .then(Commands.literal("reset")
                        .then(Commands.argument("type", StringArgumentType.word())
                            .suggests((ctx, b) -> SharedSuggestionProvider.suggest(new String[]{"wild", "nether", "end"}, b))
                            .executes(ctx -> MaintenanceManager.executeReset(ctx, StringArgumentType.getString(ctx, "type"), null))
                            .then(Commands.argument("seed", LongArgumentType.longArg())
                                .executes(ctx -> MaintenanceManager.executeReset(ctx, StringArgumentType.getString(ctx, "type"), LongArgumentType.getLong(ctx, "seed")))
                            )
                        )
                    )
                    .then(Commands.literal("pregen")
                        .executes(ModCommands::startPregen)
                        .then(Commands.literal("stop").executes(ModCommands::stopPregen))
                    )
                    .then(Commands.literal("resetdragon")
                        .executes(ModCommands::executeResetDragon)
                    )
                )

                // 1-6. Set Center
                .then(Commands.literal("setcenter").executes(WorldCommandExecutor::setWorldCenter))
            )

            // -----------------------------------------------------
            // 2. PUBLIC GROUP
            // -----------------------------------------------------
            .then(Commands.literal("friend")
                .then(Commands.literal("add")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ModCommands::addFriend)
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ModCommands::removeFriend)
                    )
                )
            )
        );
    }

    // =================================================================
    // Helper Methods
    // =================================================================

    private static int setPermissionLevel(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        int levelInt = IntegerArgumentType.getInteger(context, "level");
        PermissionLevel newLevel = PermissionLevel.fromId(levelInt);

        PlayerInfo info = PlayerDataManager.getPlayerData(target);
        info.setPermissionLevel(newLevel);
        PlayerDataManager.saveAll(context.getSource().getServer());

        context.getSource().sendSuccess(() -> Component.translatable(
            "message.simcitymod.admin_set_success", target.getName().getString(), newLevel.name()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeTeleport(CommandContext<CommandSourceStack> context, Predicate<ServerPlayer> teleportAction) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (teleportAction.test(player)) {
            return Command.SINGLE_SUCCESS;
        }
        return 0;
    }

    private static int executeSetSpawn(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        String type = StringArgumentType.getString(context, "type");

        ResourceKey<Level> currentDimension = player.level().dimension();
        GlobalPos currentPos = GlobalPos.of(currentDimension, player.blockPosition());
        MinecraftServer server = context.getSource().getServer();

        String dimId = currentDimension.m_447358_().toString();

        switch (type) {
            case "wild":
                if (currentDimension != Level.OVERWORLD && !dimId.contains(SimDimensionType.WILD.getIdPrefix())) {
                    context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_wild_overworld_only"));
                    return 0;
                }
                ServerSpawnData.setWildSpawn(server, currentPos);
                context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.setspawn_wild_success"), true);
                break;

            case "home":
                if (currentDimension != Level.OVERWORLD) {
                    context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_home_non_overworld_only"));
                    return 0;
                }
                ServerSpawnData.setHomeSpawn(server, currentPos);
                context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.setspawn_home_success"), true);
                break;

            case "nether":
                if (currentDimension != Level.NETHER && !dimId.contains(SimDimensionType.NETHER.getIdPrefix())) {
                    context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_nether_nether_only"));
                    return 0;
                }
                ServerSpawnData.setNetherSpawn(server, currentPos);
                context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.setspawn_nether_success"), true);
                break;

            case "end":
                if (currentDimension != Level.END) {
                    context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_end_end_only"));
                    return 0;
                }
                ServerSpawnData.setEndSpawn(server, currentPos);
                context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.setspawn_end_success"), true);
                break;

            default:
                context.getSource().sendFailure(Component.translatable("message.simcitymod.setspawn_invalid"));
                return 0;
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int startPregen(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        ChunkPreGenerator.startPreGeneration(level);
        context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.pregen_start"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int stopPregen(CommandContext<CommandSourceStack> context) {
        ChunkPreGenerator.stopPreGeneration();
        context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.pregen_stop_requested"), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeResetDragon(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel endLevel = source.getServer().getLevel(Level.END);

        if (endLevel == null) {
            source.sendFailure(Component.translatable("message.simcitymod.reset_dragon_fail_no_end"));
            return 0;
        }

        EndDragonFight dragonFight = endLevel.getDragonFight();
        if (dragonFight == null) {
            source.sendFailure(Component.translatable("message.simcitymod.reset_dragon_fail_no_fight"));
            return 0;
        }

        try {
            Field field = EndDragonFight.class.getDeclaredField("dragonKilled");
            field.setAccessible(true);
            field.setBoolean(dragonFight, false);

            source.sendSuccess(() -> Component.translatable("message.simcitymod.reset_dragon_success"), true);
            return Command.SINGLE_SUCCESS;

        } catch (NoSuchFieldException e) {
            source.sendFailure(Component.translatable("message.simcitymod.reset_dragon_fail_field"));
            return 0;
        } catch (Exception e) {
            source.sendFailure(Component.translatable("message.simcitymod.reset_dragon_fail_error", e.getMessage()));
            return 0;
        }
    }

    private static int addFriend(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer owner = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        if (owner.getUUID().equals(target.getUUID())) {
            context.getSource().sendFailure(Component.translatable("message.simcitymod.friend_add_self"));
            return 0;
        }

        boolean success = PlayerDataManager.addFriend(owner.getUUID(), target.getUUID(), context.getSource().getServer());

        if (success) {
            context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.friend_add_success", target.getName().getString()), true);
        } else {
            context.getSource().sendFailure(Component.translatable("message.simcitymod.friend_already_exists"));
        }
        return success ? Command.SINGLE_SUCCESS : 0;
    }

    private static int removeFriend(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer owner = context.getSource().getPlayerOrException();
        ServerPlayer target = EntityArgument.getPlayer(context, "target");

        boolean success = PlayerDataManager.removeFriend(owner.getUUID(), target.getUUID(), context.getSource().getServer());

        if (success) {
            context.getSource().sendSuccess(() -> Component.translatable("message.simcitymod.friend_remove_success", target.getName().getString()), true);
        } else {
            context.getSource().sendFailure(Component.translatable("message.simcitymod.friend_not_found"));
        }
        return success ? Command.SINGLE_SUCCESS : 0;
    }
}