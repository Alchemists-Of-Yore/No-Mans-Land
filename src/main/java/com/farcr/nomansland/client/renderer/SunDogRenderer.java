package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.Meshes;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = NoMansLand.MODID, value = Dist.CLIENT)
public class SunDogRenderer implements AutoCloseable {
    public static final SunDogRenderer INSTANCE = new SunDogRenderer();
    public static final ResourceLocation SUN_DOG_FRONT_TEXTURE = NoMansLand.location("textures/environment/sun_dog_front.png");
    public static final ResourceLocation SUN_DOG_BACK_TEXTURE = NoMansLand.location("textures/environment/sun_dog_back.png");

    public static ShaderInstance SUN_DOG_SHADER;

    @SubscribeEvent
    public static void renderLevelStage(RenderLevelStageEvent event) {
        //if (true) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        INSTANCE.render(event.getPoseStack(), event.getProjectionMatrix(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    private VertexBuffer sunDogMesh;

    public SunDogRenderer() {}

    public void render(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        if (this.sunDogMesh == null)
            this.createHemisphereMesh();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        float brightness = 0.4F;
        RenderSystem.setShaderColor(1, 1, 1, brightness);

        poseStack.pushPose();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation().conjugate());
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(Minecraft.getInstance().level.getTimeOfDay(partialTick) * 360.0F));

        float radius = 32.0F;
        radius = Math.max(radius, Minecraft.getInstance().options.renderDistance().get() * 16.0F * 0.5F);
        poseStack.scale(radius, radius, radius);

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        RenderSystem.setShaderTexture(1, target.getDepthTextureId());
        SUN_DOG_SHADER.safeGetUniform("ScreenResolution").set((float) target.width, (float) target.height);

        this.sunDogMesh.bind();

        RenderSystem.setShaderTexture(0, SUN_DOG_FRONT_TEXTURE);
        this.sunDogMesh.drawWithShader(poseStack.last().pose(), projectionMatrix, SUN_DOG_SHADER);

        poseStack.scale(2, 2, 2);
        RenderSystem.setShaderColor(1, 1, 1, brightness);
        RenderSystem.setShaderTexture(0, SUN_DOG_BACK_TEXTURE);
        this.sunDogMesh.drawWithShader(poseStack.last().pose(), projectionMatrix, SUN_DOG_SHADER);

        VertexBuffer.unbind();
        poseStack.popPose();
    }

    private void createHemisphereMesh() {
        if (this.sunDogMesh != null) this.sunDogMesh.close();

        this.sunDogMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.sunDogMesh.bind();
        this.sunDogMesh.upload(Meshes.hemisphere(Tesselator.getInstance(), 12, 24, Mth.PI * 0.3F, 1));
        VertexBuffer.unbind();
    }

    @Override
    public void close() {
        this.sunDogMesh.close();
    }
}
