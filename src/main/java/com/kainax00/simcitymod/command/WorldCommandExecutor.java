package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.config.Config;
import com.kainax00.simcitymod.data.enums.SimDimensionType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

public class WorldCommandExecutor {

    public static int setWorldCenter(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BlockPos pos = BlockPos.containing(context.getSource().getPosition());

            WorldBorder border = level.getWorldBorder();
            border.setCenter(pos.getX(), pos.getZ());

            String dimId = level.dimension().m_447358_().toString();

            double size;

            if (dimId.contains(SimDimensionType.WILD.getIdPrefix())) {
                size = Config.WILD_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.wild", size), true);
            } 
            else if (level.dimension() == Level.OVERWORLD) {
                size = Config.RESIDENTIAL_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.residential", size), true);
            } 
            else if (level.dimension() == Level.NETHER || dimId.contains(SimDimensionType.NETHER.getIdPrefix())) {
                size = Config.NETHER_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.nether", size), true);
            } 
            else if (level.dimension() == Level.END || dimId.contains(SimDimensionType.END.getIdPrefix())) {
                size = Config.END_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.end", size), true);
            } 
            else {
                size = border.getSize();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.general"), true);
            }

            border.setSize(size);
            border.setDamagePerBlock(Config.BORDER_DAMAGE.get());
            border.setWarningBlocks(Config.BORDER_WARNING.get());

            return 1;

        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}