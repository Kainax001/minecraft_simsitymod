package com.kainax00.simcitymod.manager;

import com.kainax00.simcitymod.data.MaintenanceData;
import com.kainax00.simcitymod.data.enums.SimDimensionType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class MaintenanceManager {

    /**
     * Executes the dimension reset for a specific type provided in the command.
     */
    public static int executeReset(CommandContext<CommandSourceStack> context, String typeStr, Long seed) {
        try {
            // [Fix] Manually specify the dimension type from command argument
            SimDimensionType type = SimDimensionType.valueOf(typeStr.toUpperCase());
            MinecraftServer server = context.getSource().getServer();

            // Schedule the next version (v0 -> v1 -> v2...)
            if (DynamicDimensionManager.scheduleNextDimension(server, type, seed)) {
                String seedMsg = (seed != null) ? String.valueOf(seed) : "Random";
                int nextVer = MaintenanceData.getCurrentVersion(server, type);

                // Success notification using translation keys
                context.getSource().sendSuccess(() -> Component.translatable(
                    "message.simcitymod.reset.success", type.name(), String.valueOf(nextVer), seedMsg), true);
                
                context.getSource().sendSystemMessage(Component.translatable("message.simcitymod.reset.restart_required"));
                
                return Command.SINGLE_SUCCESS;
            }
        } catch (IllegalArgumentException e) {
            // If the user types an invalid dimension type
            context.getSource().sendFailure(Component.translatable("command.simcitymod.maintenance.reset.invalid_type"));
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Reset Error: " + e.getMessage()));
        }
        return 0;
    }
}