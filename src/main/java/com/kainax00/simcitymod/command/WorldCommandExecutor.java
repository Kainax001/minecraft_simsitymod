package com.kainax00.simcitymod.command;

import com.kainax00.simcitymod.config.Config;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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

            Identifier dimensionId = level.dimension().m_447358_();
            String namespace = dimensionId.m_442187_();
            String path = dimensionId.m_445092_();

            double size;

            if (namespace.equals("simcitymod") && path.startsWith("wild_dimension")) {
                // Wild Dimension
                size = Config.WILD_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.wild", size), true);
            } 
            else if (level.dimension() == Level.OVERWORLD) {
                // Residential (Overworld)
                size = Config.RESIDENTIAL_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.residential", size), true);
            } 
            else if (level.dimension() == Level.NETHER) {
                // Nether
                size = Config.NETHER_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.nether", size), true);
            } 
            else if (level.dimension() == Level.END) {
                // The End
                size = Config.END_BORDER_SIZE.get();
                context.getSource().sendSuccess(() -> Component.translatable("simcitymod.command.admin.setcenter.success.end", size), true);
            } 
            else {
                // Others
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