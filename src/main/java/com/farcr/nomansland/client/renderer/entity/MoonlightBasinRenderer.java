package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.LinkedHashSet;
import java.util.Set;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public class MoonlightBasinRenderer implements BlockEntityRenderer<MoonlightBasinBlockEntity> {

    private static final Set<MoonlightBasinBlockEntity> PENDING_BEAMS = new LinkedHashSet<>();

    public MoonlightBasinRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(MoonlightBasinBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (blockEntity.getOfferingBeamIntensity(partialTick) < 0.01F) return;
        PENDING_BEAMS.add(blockEntity);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        if (PENDING_BEAMS.isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        for (MoonlightBasinBlockEntity blockEntity : PENDING_BEAMS)
            renderBeam(blockEntity, partialTick, poseStack, cameraPos);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();

        PENDING_BEAMS.clear();
    }

    private static void renderBeam(MoonlightBasinBlockEntity blockEntity, float partialTick, PoseStack poseStack, Vec3 cameraPos) {
        float beamIntensity = blockEntity.getOfferingBeamIntensity(partialTick);
        if (beamIntensity < 0.01F) return;

        float r = 171 / 255.0F, g = 181 / 255.0F, b = 108 / 255.0F, a = beamIntensity * 0.04F;
        float peakHeight = Math.max(blockEntity.getOfferingPeakHeight(partialTick), 1.0F);
        float fadeHeight = peakHeight + 1.5F;

        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(
                pos.getX() + 0.5 - cameraPos.x,
                pos.getY() + 1.0 - cameraPos.y,
                pos.getZ() + 0.5 - cameraPos.z
        );

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
                bufferbuilder.addVertex(poseStack.last(),  width, peakHeight, width)
                        .setColor(r, g, b, a);
                bufferbuilder.addVertex(poseStack.last(), -width, peakHeight, width)
                        .setColor(r, g, b, a);

                bufferbuilder.addVertex(poseStack.last(), -width, peakHeight, width)
                        .setColor(r, g, b, a);
                bufferbuilder.addVertex(poseStack.last(),  width, peakHeight, width)
                        .setColor(r, g, b, a);
                bufferbuilder.addVertex(poseStack.last(),  width, fadeHeight, width)
                        .setColor(0, 0, 0, a);
                bufferbuilder.addVertex(poseStack.last(), -width, fadeHeight, width)
                        .setColor(0, 0, 0, a);
            }
        }
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

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
                pos.getY() + 8,
                pos.getZ() + 1
        );
    }
}
