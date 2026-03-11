package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.tortoise.TortoiseModel;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class TortoiseRenderer extends MobRenderer<Tortoise, TortoiseModel<Tortoise>> {

    public TortoiseRenderer(EntityRendererProvider.Context context) {
        super(context, new TortoiseModel<>(context.bakeLayer(NMLModelLayers.TORTOISE_LAYER)), 0.7F);
    }

    @Override
    protected void scale(Tortoise livingEntity, PoseStack poseStack, float partialTickTime) {
        super.scale(livingEntity, poseStack, partialTickTime);
        float babyScale = 2.5F;
        if (livingEntity.isBaby()) {
            poseStack.scale(babyScale, babyScale, babyScale);
        }
    }

    @Override
    protected float getShadowRadius(Tortoise entity) {
        float shadowRadius = super.getShadowRadius(entity);
        return entity.isBaby() ? shadowRadius * 2F : shadowRadius;
    }

    @Override
    public ResourceLocation getTextureLocation(Tortoise tortoise) {
        return NoMansLand.location("textures/entity/tortoise/brown.png");
    }
}