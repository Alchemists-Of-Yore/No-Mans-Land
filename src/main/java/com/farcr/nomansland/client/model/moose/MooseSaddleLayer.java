package com.farcr.nomansland.client.model.moose;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.model.deer.DeerModel;
import com.farcr.nomansland.client.variant_action.SetAntlerLayer;
import com.farcr.nomansland.client.variant_action.SetPatternLayer;
import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.tazer.mixed_litter.VariantUtil;
import dev.tazer.mixed_litter.actions.Action;
import dev.tazer.mixed_litter.actions.VariantActionType;
import dev.tazer.mixed_litter.variants.Variant;
import dev.tazer.mixed_litter.variants.VariantType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MooseSaddleLayer extends RenderLayer<Moose, MooseModel<Moose>> {

    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/moose/moose_saddle.png");

    public MooseSaddleLayer(RenderLayerParent<Moose, MooseModel<Moose>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Moose moose, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (moose.isSaddled()) {
            getParentModel().prepareMobModel(moose, limbSwing, limbSwingAmount, partialTicks);
            VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
    }
}