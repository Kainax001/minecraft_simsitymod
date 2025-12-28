package com.kainax00.simcitymod.event;

import com.kainax00.simcitymod.SimcityMod;
import com.kainax00.simcitymod.config.Config;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SimcityMod.MOD_ID)
public class DimensionBorderHandler {

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        Identifier dimensionId = level.dimension().m_447358_();
        String namespace = dimensionId.m_442187_();
        String path = dimensionId.m_445092_();

        double size = -1.0;

        // 1. 야생 구역
        if (namespace.equals("simcitymod") && path.startsWith("wild_dimension")) {
            size = Config.WILD_BORDER_SIZE.get();
        } 
        // 2. 거주 구역 (오버월드)
        else if (level.dimension() == Level.OVERWORLD) {
            size = Config.RESIDENTIAL_BORDER_SIZE.get();
        }
        // 3. 네더
        else if (level.dimension() == Level.NETHER) {
            size = Config.NETHER_BORDER_SIZE.get();
        }
        // 4. 엔더
        else if (level.dimension() == Level.END) {
            size = Config.END_BORDER_SIZE.get();
        }

        // 5. 적용
        if (size > 0) {
            SimcityMod.LOGGER.info("Updating Border Config for " + path + " (Size: " + size + ")");
            
            WorldBorder border = level.getWorldBorder();
            border.setSize(size);
            border.setDamagePerBlock(Config.BORDER_DAMAGE.get());
            border.setWarningBlocks(Config.BORDER_WARNING.get());
        }
    }
}