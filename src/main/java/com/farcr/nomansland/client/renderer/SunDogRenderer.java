package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.Meshes;
import com.farcr.nomansland.common.worldevent.SunDog;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
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
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        INSTANCE.render(event.getPoseStack(), event.getProjectionMatrix(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }

    private VertexBuffer sunDogMesh;

    public SunDogRenderer() {}

    public void render(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        if (Minecraft.getInstance().level == null) return;
        float time = Minecraft.getInstance().level.getTimeOfDay(partialTick);
        float brightness = 0.4F;
        // fade it in
        brightness *= SunDog.Client.INSTANCE.getOpacity(partialTick);
        // no sun dogs at night!
        if (time > 0.5)
            brightness *= Mth.clampedMap(time, 0.74F, 0.76F, 0.0F, 1.0F);
        else
            brightness *= Mth.clampedMap(time, 0.23F, 0.25F, 1.0F, 0.0F);
        // nor during inclement weather
        brightness *= 1 - Minecraft.getInstance().level.getRainLevel(partialTick);

        if (brightness <= 0.0)
            return;

        if (this.sunDogMesh == null)
            this.createHemisphereMesh();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );

        RenderSystem.setShaderColor(1, 1, 1, brightness);

        poseStack.pushPose();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        poseStack.mulPose(camera.rotation().conjugate());
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(time * 360.0F));

        float radius = 32.0F;
        float renderDistance = Minecraft.getInstance().options.renderDistance().get() * 16.0F;
        radius = Math.max(radius, renderDistance * 0.3F);

        poseStack.pushPose();
        poseStack.scale(radius, radius, radius);

        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        RenderSystem.setShaderTexture(1, target.getDepthTextureId());
        SUN_DOG_SHADER.safeGetUniform("ScreenResolution").set((float) target.width, (float) target.height);

        this.sunDogMesh.bind();

        RenderSystem.setShaderTexture(0, SUN_DOG_FRONT_TEXTURE);
        this.sunDogMesh.drawWithShader(poseStack.last().pose(), projectionMatrix, SUN_DOG_SHADER);
        poseStack.popPose();

        poseStack.pushPose();

        poseStack.scale(renderDistance - 16.0F, renderDistance - 16.0F, renderDistance - 16.0F);
        RenderSystem.setShaderColor(1, 1, 1, brightness);
        RenderSystem.setShaderTexture(0, SUN_DOG_BACK_TEXTURE);
        this.sunDogMesh.drawWithShader(poseStack.last().pose(), projectionMatrix, SUN_DOG_SHADER);

        VertexBuffer.unbind();
        poseStack.popPose();
        poseStack.popPose();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    private void createHemisphereMesh() {
        if (this.sunDogMesh != null) this.sunDogMesh.close();

        this.sunDogMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.sunDogMesh.bind();
        this.sunDogMesh.upload(Meshes.hemisphere(Tesselator.getInstance(), 32, 64, Mth.PI * 0.4F, 1));
        VertexBuffer.unbind();
    }

    @Override
    public void close() {
        this.sunDogMesh.close();
    }
}
