package com.kainax00.simcitymod.client.renderer;

import com.kainax00.simcitymod.entity.TraderEntity;
import com.kainax00.simcitymod.util.IdentifierUtil;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public class TraderRenderer extends MobRenderer<TraderEntity, VillagerRenderState, VillagerModel> {

    public TraderRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), 0.5f);
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public void extractRenderState(TraderEntity entity, VillagerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.isBaby = entity.isBaby();
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState state) {
        return IdentifierUtil.parse("textures/entity/villager/plains.png");
    }
}