package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.common.entity.LivingPot;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.data.ModelData;

public class LivingPotRenderer extends EntityRenderer<LivingPot> {
    private final BlockRenderDispatcher blockRenderer;

    public LivingPotRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public ResourceLocation getTextureLocation(LivingPot livingPot) {
        return null;
    }

    @Override
    public void render(LivingPot pot, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Level level = pot.level();

        // TODO: everything else
        if (pot.variant != null) {
            ModelManager manager = this.blockRenderer.getBlockModelShaper().getModelManager();
            ModelResourceLocation location = ModelResourceLocation.standalone(pot.variant.model().withPrefix("block/"));
            BakedModel model = manager.getModel(location);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.solidBlockSheet()), pot.blockState, model, 1, 1, 1, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        }
    }
}
