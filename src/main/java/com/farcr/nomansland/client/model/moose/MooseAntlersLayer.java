package com.farcr.nomansland.client.model.moose;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.Moose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

// todo: losing antlers, antler variants
@OnlyIn(Dist.CLIENT)
public class MooseAntlersLayer extends RenderLayer<Moose, MooseModel<Moose>> {
    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/moose/moose_antlers_1.png");

    public MooseAntlersLayer(RenderLayerParent<Moose, MooseModel<Moose>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Moose moose, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, LivingEntityRenderer.getOverlayCoords(moose, 0.0F));
    }
}
