package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.common.entity.FallingPotEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;

public class FallingPotRenderer extends EntityRenderer<FallingPotEntity> {

    private final BlockRenderDispatcher blockRenderer;

    public FallingPotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5F;
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(FallingPotEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        ResourceLocation model = entity.getVariantModel();
        if (model == null) return;

        poseStack.pushPose();

        Direction direction = Direction.NORTH;
        try {
            direction = entity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        } catch (IllegalArgumentException ignored) {}

        poseStack.translate(-0.5, 0, -0.5);
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - direction.toYRot()));
        poseStack.translate(-0.5, 0, -0.5);

        ModelManager manager = blockRenderer.getBlockModelShaper().getModelManager();
        ModelResourceLocation location = ModelResourceLocation.standalone(model.withPrefix("block/"));
        BakedModel bakedModel = manager.getModel(location);
        blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.solidBlockSheet()), entity.getBlockState(), bakedModel, 1.0F, 1.0F, 1.0F, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FallingPotEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/atlas/blocks.png");
    }
}
