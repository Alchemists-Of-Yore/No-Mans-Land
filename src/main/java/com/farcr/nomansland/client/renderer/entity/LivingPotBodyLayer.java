package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.client.model.living_pot.LivingPotModel;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Renders the pot's block model as the entity's body, positioned at the animated "body" bone.
 * The legs are rendered separately by LivingPotModel; this layer overlays the block model on top.
 */
@OnlyIn(Dist.CLIENT)
public class LivingPotBodyLayer extends RenderLayer<LivingPot, LivingPotModel<LivingPot>> {

    private final BlockRenderDispatcher blockRenderer;

    public LivingPotBodyLayer(RenderLayerParent<LivingPot, LivingPotModel<LivingPot>> renderer, BlockRenderDispatcher blockRenderer) {
        super(renderer);
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingPot entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ResourceLocation variantLoc = ResourceLocation.tryParse(entity.getEntityData().get(LivingPot.VARIANT));
        if (variantLoc == null) return;
        PotVariant variant = entity.level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).get(variantLoc);
        BlockState state = entity.getEntityData().get(LivingPot.BLOCKSTATE);
        if (variant == null) return;

        LivingPotModel<LivingPot> model = getParentModel();

        poseStack.pushPose();

        // Apply animated bone transforms so the block model follows body animations
//        model.getBone().translateAndRotate(poseStack);

        model.getBody().translateAndRotate(poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        poseStack.translate(-0.5, -1.2, -0.5);

        // Convert from model space to block rendering space.
        // The body bone is at (0, -4.6667, 0) in model units (1/16 of a block).
        // After translateAndRotate, we're at the body bone origin.
        // We need to offset so the block model renders centered on this bone.
        // Block models render from (0,0,0) to (1,1,1), so translate by (-0.5, 0, -0.5)
        // to center horizontally. The vertical offset accounts for the body bone's position.
//        poseStack.scale(1.0F, 1.0F, 1.0F);
//        poseStack.translate(-8.0F, -16.0F, -8.0F);

        // Use alive model if present, otherwise fall back to the regular model
        ResourceLocation modelLoc = variant.aliveModel().orElse(variant.model());
        ModelManager manager = blockRenderer.getBlockModelShaper().getModelManager();
        ModelResourceLocation mrl = ModelResourceLocation.standalone(modelLoc.withPrefix("block/"));
        BakedModel bakedModel = manager.getModel(mrl);

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(Sheets.solidBlockSheet()),
                state,
                bakedModel,
                1.0F, 1.0F, 1.0F,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );

        poseStack.popPose();
    }
}
