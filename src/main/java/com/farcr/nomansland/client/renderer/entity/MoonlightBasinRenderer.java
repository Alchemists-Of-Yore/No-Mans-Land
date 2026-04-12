package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.friend.FriendMoonState;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public class MoonlightBasinRenderer implements BlockEntityRenderer<MoonlightBasinBlockEntity> {

    public MoonlightBasinRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(MoonlightBasinBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float beamIntensity = blockEntity.getOfferingBeamIntensity(partialTick);
        if (beamIntensity < 0.01F) return;

        float r = 171 / 255.0F, g = 181 / 255.0F, b = 108 / 255.0F, a = beamIntensity * 0.04F;
        float startGradientHeight = 8, endGradientHeight = 15;

        poseStack.pushPose();
        poseStack.translate(0.5, 1.0, 0.5);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 4; i++) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
            for (int j = 2; j >= 0; j--) {
                float factor = (float) j / 2;
                float width = Mth.lerp(factor, 0.3F, 0.1F);

                bufferbuilder.addVertex(poseStack.last(), -width, 0, width)
                        .setColor(0, 0, 0, a);
                bufferbuilder.addVertex(poseStack.last(),  width, 0, width)
                        .setColor(0, 0, 0, a);
                bufferbuilder.addVertex(poseStack.last(),  width,  startGradientHeight, width)
                        .setColor(r, g, b, a);
                bufferbuilder.addVertex(poseStack.last(), -width,  startGradientHeight, width)
                        .setColor(r, g, b, a);

                bufferbuilder.addVertex(poseStack.last(), -width, startGradientHeight, width)
                        .setColor(r, g, b, a);
                bufferbuilder.addVertex(poseStack.last(),  width, startGradientHeight, width)
                        .setColor(r, g, b, a);
                bufferbuilder.addVertex(poseStack.last(),  width,  endGradientHeight, width)
                        .setColor(0, 0, 0, a);
                bufferbuilder.addVertex(poseStack.last(), -width,  endGradientHeight, width)
                        .setColor(0, 0, 0, a);
            }
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

        RenderSystem.depthMask(true);
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MoonlightBasinBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 1,
                pos.getY() + 16,
                pos.getZ() + 1
        );
    }
}
