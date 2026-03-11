package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.neoforged.neoforge.client.model.data.ModelData;

public class PotRenderer<T extends PotBlockEntity> implements BlockEntityRenderer<T> {
    private final BlockRenderDispatcher blockRenderer;

    public PotRenderer(BlockEntityRendererProvider.Context context) {
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(T pot, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        Direction direction = pot.getDirection();
        poseStack.translate(0.5F, 0, (double)0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - direction.toYRot()));
        poseStack.translate(-0.5F, 0, (double)-0.5F);
        DecoratedPotBlockEntity.WobbleStyle wobbleStyle = pot.lastWobbleStyle;
        if (wobbleStyle != null && pot.getLevel() != null) {
            float progress = ((float)(pot.getLevel().getGameTime() - pot.wobbleStartedAtTick) + partialTick) / (float) wobbleStyle.duration;
            if (progress >= 0 && progress <= 1) {
                if (wobbleStyle == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                    float strength = 1/64F;
                    float angle = progress * ((float) Math.PI * 2F);
                    float xRot = -1.5F * (Mth.cos(angle) + 0.5F) * Mth.sin(angle / 2);
                    poseStack.rotateAround(Axis.XP.rotation(xRot * strength), 0.5F, 0, 0.5F);
                    float zRot = Mth.sin(angle);
                    poseStack.rotateAround(Axis.ZP.rotation(zRot * strength), 0.5F, 0, 0.5F);
                } else {
                    float yRot = Mth.sin(-progress * 3 * (float) Math.PI) * 0.125F;
                    float fadeOut = 1 - progress;
                    poseStack.rotateAround(Axis.YP.rotation(yRot * fadeOut), 0.5F, 0, 0.5F);
                }
            }
        }

        if (pot.variant != null) {
            ModelManager manager = this.blockRenderer.getBlockModelShaper().getModelManager();
            ModelResourceLocation location = ModelResourceLocation.standalone(pot.variant.model().withPrefix("block/"));
            BakedModel model = manager.getModel(location);
            blockRenderer.getModelRenderer().renderModel(poseStack.last(), bufferSource.getBuffer(Sheets.solidBlockSheet()), pot.getBlockState(), model, 1, 1, 1, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        }

        poseStack.popPose();
    }
}
