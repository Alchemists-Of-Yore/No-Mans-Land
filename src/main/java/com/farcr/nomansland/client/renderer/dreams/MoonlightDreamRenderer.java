package com.farcr.nomansland.client.renderer.dreams;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.Meshes;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import com.farcr.nomansland.common.friend.FriendMoonUpdate;
import com.farcr.nomansland.common.networking.friend.FriendMoonUpdatePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MoonlightDreamRenderer implements IDreamRenderer {
    public static ShaderInstance DREAM_SKY_SHADER;

    public Quaternionf skyRotation = Axis.XP.rotationDegrees(35);

    public int getGradientColor() {
        return FastColor.ARGB32.color(
            168, 143, 87
        );
    }

    public boolean render(
        LevelRenderer levelRenderer,
        PoseStack poseStack,
        DeltaTracker deltaTracker,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix
    ) {
        RenderSystem.depthMask(false);
        poseStack.mulPose(frustumMatrix);
        poseStack.mulPose(skyRotation);

        float c0 = RenderSystem.getShaderColor()[0];
        float c1 = RenderSystem.getShaderColor()[1];
        float c2 = RenderSystem.getShaderColor()[2];
        float c3 = RenderSystem.getShaderColor()[3];

        float starAlpha = getStarBrightness(0f, 0f);
        RenderSystem.setShaderColor(
            ((float) FastColor.ARGB32.red(getGradientColor()) / 255f),
            ((float) FastColor.ARGB32.green(getGradientColor()) / 255f),
            ((float)FastColor.ARGB32.blue(getGradientColor()) / 255f),
            starAlpha
        );

        RenderSystem.defaultBlendFunc();
        renderDream(poseStack, deltaTracker, projectionMatrix);

        if (levelRenderer.starBuffer == null)
            levelRenderer.createStars();

        FriendMoonRenderer.applySkyBlendFunction();
        RenderSystem.disableBlend();
        levelRenderer.starBuffer.bind();
        levelRenderer.starBuffer.drawWithShader(poseStack.last().pose(),
            projectionMatrix, GameRenderer.getPositionShader());
        VertexBuffer.unbind();

        RenderSystem.setShaderColor(c0, c1, c2, c3);
        renderMoon(poseStack, projectionMatrix);

        levelRenderer.renderBuffers.bufferSource().endLastBatch();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        FogRenderer.setupNoFog();

        return true;
    }

    public float getStarBrightness(float partialTick, float originalBrightness) {
        DreamType dreamType = ClientDreamRenderer.getInstance().getDream();
        double distance = MoonlightDreamType.BASIN_POSITION.distSqr(
            new Vec3i(
                (int) dreamType.spawnPoint.x,
                (int) dreamType.spawnPoint.y,
                (int) dreamType.spawnPoint.z
            )
        );

        Vec3 playerPosition = Minecraft.getInstance().player.position();
        double currentDistance = MoonlightDreamType.BASIN_POSITION.distSqr(
            new Vec3i(
                (int) playerPosition.x,
                (int) playerPosition.y,
                (int) playerPosition.z
            )
        );

        return (1.f - (float) Math.clamp(currentDistance / distance, 0, 1));
    }

    private boolean hasSeenMoon = false;

    public float elapsedTime = 0.0f;
    private float ticksSinceSeenMoon = 0.0f;
    public void renderDream(
        PoseStack poseStack, DeltaTracker deltaTracker, Matrix4f projectionMatrix
    ) {
        poseStack.pushPose();
        poseStack.scale(100f, 100f, 100f);

        RenderSystem.enableBlend();
        VertexBuffer skyBuffer = getSkyMesh();

        elapsedTime += (deltaTracker.getGameTimeDeltaTicks() / 40) ;

        MoonlightDreamType dream = (MoonlightDreamType) ClientDreamRenderer.getInstance().getDream();

        float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
        float timeWithDelta = dream.moonPresenceTime + partialTicks;
        if (hasSeenMoon) ticksSinceSeenMoon += partialTicks;
        timeWithDelta = Math.max(timeWithDelta - 1f, 0);
        float speed = 1 / 150f;

        if (timeWithDelta > 0) {
            Entity camera = Minecraft.getInstance().getCameraEntity();

            Vec3 camPos = camera.getEyePosition(partialTicks);
            Vector3f targetPosition = camPos.toVector3f().add(new Vector3f(0, 100, 0).rotate(skyRotation));

            Vec3 target = new Vec3(targetPosition.x, targetPosition.y, targetPosition.z);
            Vec3 dir = target.subtract(camPos).normalize();

            float yawTo = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90f;
            float pitchTo = (float) Math.toDegrees(-Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)));

            float yaw = camera.getYRot();
            float pitch = camera.getXRot();

            float rotateSpeed = 0.08f * (speed * timeWithDelta / 2f);
            if (hasSeenMoon) rotateSpeed = Math.min(rotateSpeed, 0.5f);
            camera.setYRot(yaw + (Mth.wrapDegrees(yawTo - yaw) * rotateSpeed));
            camera.setXRot(pitch + ((pitchTo - pitch) * rotateSpeed));
        }

        float distance = Math.max(0.25f, getStarBrightness(0f, 0f));
        DREAM_SKY_SHADER.safeGetUniform("CornerFade").set((timeWithDelta * speed));
        DREAM_SKY_SHADER.safeGetUniform("Intensity").set((.5f * distance) + (timeWithDelta * speed));
        DREAM_SKY_SHADER.safeGetUniform("Time").set(elapsedTime);

        skyBuffer.drawWithShader(poseStack.last().pose(), projectionMatrix, DREAM_SKY_SHADER);

        VertexBuffer.unbind();
        poseStack.popPose();
    }

    private static final int MOON_FADE_START_TIME = 50;
    private static final int STARE_AT_MOON_TICKS = 30;
    @Override public float getFadeAlpha(float originalAlpha) {
        if (hasSeenMoon && ticksSinceSeenMoon > 0f)
            return Math.clamp(((ticksSinceSeenMoon - (MOON_FADE_START_TIME + STARE_AT_MOON_TICKS)) / MoonlightDreamType.MAX_MOON_GAZE_TIME), 0, 1);
        return originalAlpha;
    }

    public void renderMoon(PoseStack poseStack, Matrix4f projectionMatrix) {
        RenderSystem.enableBlend();
        FriendMoonRenderer.applyMultiplyBlendFunction();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f moonViewMatrix = poseStack.last().pose();

        if (!hasSeenMoon && FriendMoonRenderer.moonOnScreen(Minecraft.getInstance(),
            moonViewMatrix, projectionMatrix, FriendMoonRenderer.LOOKING_AT_THRESHOLD)
        && ((MoonlightDreamType) ClientDreamRenderer.getInstance().getDream()).moonPresenceTime > 0) {
            if (ticksSinceSeenMoon > STARE_AT_MOON_TICKS) FriendMoonUpdatePacket.toServer(FriendMoonUpdate.ToServer.SAW_MOON_IN_DREAM);
            hasSeenMoon = true;
        }
        FriendMoonRenderer.FriendMoonAnimation animation = (hasSeenMoon && (ticksSinceSeenMoon > STARE_AT_MOON_TICKS))
            ? FriendMoonRenderer.FriendMoonAnimation.DREAM
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
