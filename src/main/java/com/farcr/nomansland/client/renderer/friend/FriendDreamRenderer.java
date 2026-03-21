package com.farcr.nomansland.client.renderer.friend;

import com.farcr.nomansland.client.Meshes;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

public class FriendDreamRenderer implements AutoCloseable {
    public static ShaderInstance DREAM_SKY_SHADER;
    public static FriendDreamRenderer INSTANCE = new FriendDreamRenderer();
    public static FriendDreamRenderer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FriendDreamRenderer();
        return INSTANCE;
    }

    public static void destroy() {
        if (INSTANCE == null)
            return;
        INSTANCE.close();
        INSTANCE = null;
    }

    public float elapsedTime = 0.0f;
    public void renderDream(
        LevelRenderer levelRenderer, PoseStack poseStack,
        DeltaTracker deltaTracker, boolean renderBlockOutline,
        Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
        Matrix4f frustumMatrix, Matrix4f projectionMatrix
    ) {
        RenderSystem.defaultBlendFunc();
        poseStack.scale(100f, 100f, 100f);
//        poseStack.mulPose(Axis.ZP.rotationDegrees(180));

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1f);
        VertexBuffer skyBuffer = getSkyMesh();

        elapsedTime += (deltaTracker.getGameTimeDeltaTicks() / 40) ;
        DREAM_SKY_SHADER.safeGetUniform("Intensity").set(.1f);
        DREAM_SKY_SHADER.safeGetUniform("Time").set(elapsedTime);
//        for (int i = 0; i < 2; i++) {
            skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, DREAM_SKY_SHADER);
//            poseStack.scale(1f, -1f, 1f);
//        }
        VertexBuffer.unbind();
        poseStack.popPose();
        FriendMoonRenderer.applySkyBlendFunction();
    }

    public void renderMoon(PoseStack poseStack, Matrix4f projectionMatrix) {
        FriendMoonRenderer.applyMultiplyBlendFunction();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f moonViewMatrix = poseStack.last().pose();

//        moonViewMatrix.billboardSpherical(
//            moonViewMatrix.transformPosition(0f, FriendMoonRenderer.MOON_DISTANCE, 0F, new Vector3f()),
//            new Vector3f(0f, 0f, 0f)
//        );

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
            skyMesh.upload(Meshes.texturelessHemisphere(Tesselator.getInstance(), 24, 24, Mth.PI * 0.55F, 1,
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
