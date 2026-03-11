package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.BillhookBassModel;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class BillhookBassRenderer extends MobRenderer<BillhookBass, BillhookBassModel<BillhookBass>> {
    public BillhookBassRenderer(EntityRendererProvider.Context context) {
        super(context, new BillhookBassModel<>(context.bakeLayer(NMLModelLayers.BASS_LAYER)), 0.4F);
    }

    @Override
    protected void setupRotations(BillhookBass billhookBass, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(billhookBass, poseStack, bob, yBodyRot, partialTick, scale);
        float f = 1.0F;
        float f1 = 1.0F;
        if (!billhookBass.isInWater()) {
            f = 1.3F;
            f1 = 1.7F;
        }

        float f2 = f * 4.3F * Mth.sin(f1 * 0.6F * bob);
        poseStack.mulPose(Axis.YP.rotationDegrees(f2));
        poseStack.translate(0.0F, 0.0F, -0.4F);
        if (!billhookBass.isInWater()) {
            poseStack.translate(0.2F, 0.1F, 0.0F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BillhookBass billhookBass) {
        return NoMansLand.location("textures/entity/billhook_bass/billhook_bass_amberscale.png");
    }
}
