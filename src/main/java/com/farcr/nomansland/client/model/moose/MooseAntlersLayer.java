package com.farcr.nomansland.client.model.moose;

import com.farcr.nomansland.client.variant_action.SetAntlerLayer;
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
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MooseAntlersLayer extends RenderLayer<Moose, MooseModel<Moose>> {

    public MooseAntlersLayer(RenderLayerParent<Moose, MooseModel<Moose>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Moose moose, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (moose.hasAntlers()) {
            ResourceLocation texture = null;

            for (Variant variant : VariantUtil.getVariants(moose)) {
                VariantType variantType = VariantUtil.getType(moose, variant);
                for (Action action : variantType.actions()) {
                    VariantActionType actionType = action.type();

                    actionType.resolve(action.arguments(), variant.arguments(), variantType.defaults());

                    if (actionType instanceof SetAntlerLayer setAntlerLayer) {
                        texture = setAntlerLayer.texture;
                    }
                }
            }

            if (texture != null) {
                int overlay = LivingEntityRenderer.getOverlayCoords(moose, 0.0F);
                getParentModel().prepareMobModel(moose, limbSwing, limbSwingAmount, partialTicks);
                VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
                getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, overlay);
            }
        }
    }
}