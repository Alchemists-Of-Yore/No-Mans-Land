package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.client.Meshes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class UpperAtmosphericRenderer {
    public static final UpperAtmosphericRenderer INSTANCE = new UpperAtmosphericRenderer();
    public static ShaderInstance UPPER_ATMOSPHERE_SHADER;

    private VertexBuffer ozoneMesh;

    public void render(PoseStack poseStack, Matrix4f projectionMatrix, float skyR, float skyG, float skyB, float partialTick) {
        this.createOzoneMesh();

        poseStack.pushPose();
        poseStack.scale(100.0F, 100.0F, 100.0F);
        float alpha = 0.0F;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (Minecraft.getInstance().level != null)
            alpha = this.getUpperAtmosphereFactor(camera.getPosition().y());

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        UPPER_ATMOSPHERE_SHADER.safeGetUniform("SkyColor").set(skyR, skyG, skyB, 1.0F);
        this.ozoneMesh.bind();
        this.ozoneMesh.drawWithShader(poseStack.last().pose(), projectionMatrix, UPPER_ATMOSPHERE_SHADER);
        VertexBuffer.unbind();
        poseStack.popPose();
    }

    public float getUpperAtmosphereFactor(double y) {
        return Mth.clampedMap((float) y, 180, 256, 0, 1) * (1 - FriendMoonRenderer.getFriendMoonOpacity());
    }

    private void createOzoneMesh() {
        if (this.ozoneMesh != null) this.ozoneMesh.close();

        this.ozoneMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        this.ozoneMesh.bind();
        this.ozoneMesh.upload(Meshes.texturelessHemisphere(Tesselator.getInstance(), 24, 24, Mth.PI * 0.55F, 1,
                1F, 1F, 1F, 0F,
                0.3F, 0.3F, 0.4F, 1F,
                0.2F, -0.25F));
        VertexBuffer.unbind();
    }
}
