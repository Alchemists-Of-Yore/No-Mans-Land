package com.farcr.nomansland.client.model.living_pot;

import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class LivingPotBodyLayer extends RenderLayer<LivingPot, LivingPotModel<LivingPot>> {

    private final BlockRenderDispatcher blockRenderer;

    public LivingPotBodyLayer(RenderLayerParent<LivingPot, LivingPotModel<LivingPot>> renderer, BlockRenderDispatcher blockRenderer) {
        super(renderer);
        this.blockRenderer = blockRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingPot pot, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        PotVariant variant = pot.getVariant();
        if (variant == null) return;
        BlockState state = pot.getBlockState();

        LivingPotModel<LivingPot> model = getParentModel();

        poseStack.pushPose();

        model.getBody().translateAndRotate(poseStack);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        double yOffset = pot.isLarge() ? -1.0 / 16.0 : 0;
        poseStack.translate(-0.5, yOffset, -0.5);

        ResourceLocation modelLoc = variant.model();
        ModelManager manager = blockRenderer.getBlockModelShaper().getModelManager();
        ModelResourceLocation mrl = ModelResourceLocation.standalone(modelLoc.withPrefix("block/"));
        BakedModel bakedModel = manager.getModel(mrl);

        int overlay = LivingEntityRenderer.getOverlayCoords(pot, 0.0F);

        blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(Sheets.solidBlockSheet()),
                state,
                bakedModel,
                1.0F, 1.0F, 1.0F,
                packedLight,
                overlay,
                ModelData.EMPTY,
                RenderType.cutout()
        );

        poseStack.popPose();
    }
}
