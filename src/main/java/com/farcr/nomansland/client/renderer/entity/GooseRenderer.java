package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.goose.GooseCarryLayer;
import com.farcr.nomansland.client.model.goose.GooseModel;
import com.farcr.nomansland.common.entity.goose.Goose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.tazer.mixed_litter.VariantUtil;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GooseRenderer extends MobRenderer<Goose, GooseModel<Goose>> {
    public GooseRenderer(EntityRendererProvider.Context context) {
        super(context, new GooseModel<>(context.bakeLayer(NMLModelLayers.GOOSE_LAYER)), 0.5F);
        addLayer(new GooseCarryLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void setupRotations(Goose goose, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(goose, poseStack, bob, yBodyRot, partialTick, scale);
        if (goose.isFlying()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(goose.getFlightRoll(partialTick)));
            poseStack.mulPose(Axis.XP.rotationDegrees(goose.getFlightPitch(partialTick)));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Goose goose) {
        return VariantUtil.resolveTexture(goose, NoMansLand.location("textures/entity/goose/goose_barnacle.png"), false);
    }
}
