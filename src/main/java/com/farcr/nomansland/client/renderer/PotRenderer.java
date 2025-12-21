package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.model.data.ModelData;

public class PotRenderer<T extends PotBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;

    public PotRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(T pot, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (pot.variant != null) {
            ModelManager manager = this.blockRenderer.getBlockModelShaper().getModelManager();
            ModelResourceLocation location = ModelResourceLocation.standalone(pot.variant.model().withPrefix("block/"));
            BakedModel model = manager.getModel(location);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.solidBlockSheet()), pot.getBlockState(), model, 1, 1, 1, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.CUTOUT);
        }
    }
}
