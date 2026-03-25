package com.farcr.nomansland.client.renderer.dreams;

import com.farcr.nomansland.client.Meshes;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class MoonlightDreamRenderer extends AbstractDreamRenderer {
    public static ShaderInstance DREAM_SKY_SHADER;

    public boolean render(
        LevelRenderer levelRenderer,
        PoseStack poseStack,
        DeltaTracker deltaTracker,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix
    ) {
        RenderSystem.depthMask(false);
        poseStack.mulPose(frustumMatrix);

        poseStack.mulPose(Axis.XP.rotationDegrees(35));
        renderDream(poseStack, deltaTracker, projectionMatrix);

        if (levelRenderer.starBuffer == null)
            levelRenderer.createStars();
        levelRenderer.starBuffer.bind();
        levelRenderer.starBuffer.drawWithShader(poseStack.last().pose(),
            projectionMatrix, GameRenderer.getPositionShader());
        VertexBuffer.unbind();

        renderMoon(poseStack, projectionMatrix);

        levelRenderer.renderBuffers.bufferSource().endLastBatch();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        FogRenderer.setupNoFog();

        return true;
    }

    public float elapsedTime = 0.0f;
    public void renderDream(
        PoseStack poseStack, DeltaTracker deltaTracker, Matrix4f projectionMatrix
    ) {
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        poseStack.scale(100f, 100f, 100f);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1f);
        VertexBuffer skyBuffer = getSkyMesh();

        elapsedTime += (deltaTracker.getGameTimeDeltaTicks() / 40) ;
        DREAM_SKY_SHADER.safeGetUniform("Intensity").set(.5f);
        DREAM_SKY_SHADER.safeGetUniform("Time").set(elapsedTime);

        skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, DREAM_SKY_SHADER);

        VertexBuffer.unbind();
        poseStack.popPose();
        FriendMoonRenderer.applySkyBlendFunction();
    }

    public void renderMoon(PoseStack poseStack, Matrix4f projectionMatrix) {
        FriendMoonRenderer.applyMultiplyBlendFunction();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f moonViewMatrix = poseStack.last().pose();

        boolean moonIsVisible = FriendMoonRenderer.moonOnScreen(Minecraft.getInstance(),
            moonViewMatrix, projectionMatrix, FriendMoonRenderer.LOOKING_AT_THRESHOLD);
        FriendMoonRenderer.FriendMoonAnimation animation = moonIsVisible ? FriendMoonRenderer.FriendMoonAnimation.DREAM
            : FriendMoonRenderer.FriendMoonAnimation.DREAM_UNFOCUSED;
        FriendMoonRenderer.renderFriendMoonInternal(Tesselator.getInstance(), moonViewMatrix,
            animation, 1f, 0, false);
    }

    private VertexBuffer skyMesh;
    private VertexBuffer getSkyMesh() {
        if (skyMesh == null) {
            skyMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
            skyMesh.bind();
            skyMesh.upload(Meshes.texturelessHemisphere(Tesselator.getInstance(),
                24, 24, Mth.PI * 0.55F, 1,
                1F, 1F, 1F, 0F,
                0.3F, 0.3F, 0.4F, 1F,
                0.2F, -0.25F));
            VertexBuffer.unbind();
        }
        skyMesh.bind();
        return skyMesh;
    }

    @Override
    public void close() {
        if (skyMesh != null) {
            skyMesh.close();
            skyMesh = null;
        }
    }
}
