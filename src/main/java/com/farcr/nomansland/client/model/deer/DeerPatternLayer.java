package com.farcr.nomansland.client.model.deer;

import com.farcr.nomansland.client.variant_action.SetPatternLayer;
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
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeerPatternLayer extends RenderLayer<Deer, DeerModel<Deer>> {

    public DeerPatternLayer(RenderLayerParent<Deer, DeerModel<Deer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, Deer deer, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ResourceLocation texture = null;

        for (Variant variant : VariantUtil.getVariants(deer)) {
            VariantType variantType = VariantUtil.getType(deer, variant);
            for (Action action : variantType.actions()) {
                VariantActionType actionType = action.type();

                actionType.resolve(action.arguments(), variant.arguments(), variantType.defaults());

                if (actionType instanceof SetPatternLayer setPatternLayer) {
                    texture = deer.isBaby() ? setPatternLayer.babyTexture : setPatternLayer.texture;
                }
            }
        }

        if (texture != null) {
            int overlay = LivingEntityRenderer.getOverlayCoords(deer, 0.0F);
            getParentModel().prepareMobModel(deer, limbSwing, limbSwingAmount, partialTicks);
            VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
            getParentModel().renderToBuffer(poseStack, vertexconsumer, packedLight, overlay);
        }
    }
}
