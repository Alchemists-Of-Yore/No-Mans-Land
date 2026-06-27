package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.BuriedModel;
import com.farcr.nomansland.common.entity.BuriedEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class BuriedRenderer extends MobRenderer<BuriedEntity, BuriedModel<BuriedEntity>> {

    public BuriedRenderer(EntityRendererProvider.Context context) {
        super(context, new BuriedModel<>(context.bakeLayer(NMLModelLayers.BURIED_LAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    protected void setupRotations(BuriedEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);
        poseStack.translate(0.0F, 0.6F, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
    }

    @Override
    public ResourceLocation getTextureLocation(BuriedEntity entity) {
        return NoMansLand.location("textures/entity/buried/buried_0.png");
    }
}
