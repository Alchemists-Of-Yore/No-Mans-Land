package com.farcr.nomansland.client.model.deer;

import com.farcr.nomansland.client.variant_action.SetAntlerLayer;
import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
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
public class DeerAntlersLayer extends RenderLayer<Deer, DeerModel<Deer>> {

    public DeerAntlersLayer(RenderLayerParent<Deer, DeerModel<Deer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Deer deer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (deer.hasAntlers()) {
            ResourceLocation texture = null;

            for (Variant variant : VariantUtil.getVariants(deer)) {
                VariantType variantType = VariantUtil.getType(deer, variant);
                for (Action action : variantType.actions()) {
                    VariantActionType actionType = action.type();

                    actionType.initialize(action.arguments(), variant.arguments(), variantType.defaults());

                    if (actionType instanceof SetAntlerLayer setAntlerLayer) {
                        texture = setAntlerLayer.texture;
                    }
                }
            }

            if (texture != null) {
                getParentModel().prepareMobModel(deer, limbSwing, limbSwingAmount, partialTicks);
                VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
                getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
