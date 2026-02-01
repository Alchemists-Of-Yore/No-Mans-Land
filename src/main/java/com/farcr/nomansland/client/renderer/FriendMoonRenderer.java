package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.networking.ServerboundFriendMoonUpdatePacket;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.*;

import javax.annotation.Nullable;
import java.lang.Math;
import java.util.Optional;
import java.util.function.IntFunction;

public class FriendMoonRenderer {
    public enum FriendMoonAnimation {
        TALKING(0, "talking", 1, 2),
        SURPRISED(1, "surprised", 1, 1),
        HIDDEN(2, "hidden", 1, 1),
        HIDDEN_2(3, "hidden_unfocused", 1, 1),
        PHASES(4, "phases", 6, 1);

        public static final String textureLocation = "textures/misc/friendmoon_";
        private final ResourceLocation location;

        private final String name;
        private final int xFrames;
        private final int yFrames;
        private final int id;

        FriendMoonAnimation(int id, String name, int xFrames, int yFrames) {
            this.location = NoMansLand.location(textureLocation + name + ".png");

            this.id = id;
            this.name = name;
            this.xFrames = xFrames;
            this.yFrames = yFrames;
        }

        public int getFrames() {
            return (xFrames * yFrames) - 1;
        }

        public float[] getUV(int frame) {
            int horizontalIndex = frame % xFrames;
            int verticalIndex = (frame / xFrames) % yFrames;

            return new float[] {
                horizontalIndex / (float) xFrames, (verticalIndex + 1) / (float) yFrames,
                (horizontalIndex + 1) / (float) xFrames, verticalIndex / (float) yFrames
            };
        }

        public ResourceLocation getLocation() {
            return location;
        }

        public int getId() {
            return id;
        }

        public static final IntFunction<FriendMoonAnimation> BY_ID = ByIdMap.continuous(FriendMoonAnimation::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, FriendMoonAnimation> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FriendMoonAnimation::getId);
    }

    public static float friendMoonOpacity = 0.0f;
    public static float getFriendMoonOpacity() {
        return friendMoonOpacity;
    }

    public static final float MOON_SIZE = 20f;
    public static final float MOON_DISTANCE = 50f;

    public static FriendMoonAnimation MOON_ANIMATION = FriendMoonAnimation.TALKING;
    public static void setFriendMoonAnimation(FriendMoonAnimation newAnimation) {
        if (MOON_ANIMATION != newAnimation)
            animationProgress = 0;
        MOON_ANIMATION = newAnimation;
    }
    public static FriendMoonAnimation EMOTION = FriendMoonAnimation.TALKING;
    public static void setFriendMoonEmotion(FriendMoonAnimation newState) {
        EMOTION = newState;
    }

    public static double getSkyAngle(Level level, float partialTick) {
        return -(level.getTimeOfDay(partialTick) * Math.TAU) - (Math.PI / 2f);
    }

    public static float friendMoonPitchAngle = 0f;
    public static float friendMoonYawAngle = 0f;

    public static boolean moonOnScreen(Minecraft mc, Matrix4f moonViewMatrix, float partialTick) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vector3f worldPosition = moonViewMatrix.transformPosition(0f, MOON_DISTANCE, 0F, new Vector3f());
        Vector4f clip = new Vector4f(worldPosition, 1f).mul(
            mc.gameRenderer.getProjectionMatrix(mc.gameRenderer.getFov(camera, partialTick, true))
        );
        return (clip.w > 0.0f)
            && ((Math.abs(clip.x / clip.w) <= 1)
            && (Math.abs(clip.y / clip.w) <= 1));
    }

    public static Vector3f euler(Vector3f direction) {
        // Abstract to XZ plane and calculate angle as if 2D
        Vector3f yawVector = new Vector3f(direction.x, 0f, direction.z);
        yawVector.normalize();

        // Abstract to Y/XZ plane and use the length of XZ
        Vector3f pitchVector = new Vector3f(new Vector2f(direction.x, direction.z).length(), direction.y, 0f);
        pitchVector.normalize();

        // Calculate angles
        float yaw = (float)Math.atan2(yawVector.z, yawVector.x);
        float pitch = (float)Math.atan2(pitchVector.y, pitchVector.x);

        return new Vector3f(Float.isNaN(pitch) ? 0f : pitch, Float.isNaN(yaw) ? 0f : yaw, 0f);
    }

    public static BlockPos clientBlockPos;

    // Retrieve Animation Type Logic
    private static FriendMoonAnimation getFriendMoonAnimation() {
        return MOON_ANIMATION;
    }

    public static float animationProgress = 0;
    public static void updateFriendMoonPosition(Entity cameraEntity, Matrix4f moonViewMatrix, float partialTick) {
        // Temporary Wake Up Logic
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        boolean fadeOut = true;
        float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
        float fadeSpeed = deltaTime / 20f;
        float animateSpeed = deltaTime / 5f;

        if (clientBlockPos != null) {
            assert player != null;
            Optional<MoonlightBasinBlockEntity> basinEntity = player.level().getBlockEntity(clientBlockPos, NMLBlockEntities.MOONLIGHT_BASIN.get());
            // Can stare up at the moon and it'll show up
            if (player.hasEffect(NMLEffects.FRIENDSHIP) && (basinEntity.isPresent() && basinEntity.get().timeAllowed)) {
                MoonlightBasinBlockEntity basin = basinEntity.get();
                // temporary just "moon on screen" i will replace with facing upwards
                if (moonOnScreen(mc, moonViewMatrix, partialTick) || basin.moonAwake) {
                    fadeOut = false;
                    friendMoonOpacity = Math.min(friendMoonOpacity + fadeSpeed, 1);
                    // moon awakening logic
                    if (!basin.moonAwake) {
                        // moon rotation
                        if (friendMoonOpacity >= 1) {
                            animationProgress = Math.min(animationProgress + animateSpeed, getFriendMoonAnimation().getFrames());
                            if (animationProgress >= getFriendMoonAnimation().getFrames()) {
                                PacketDistributor.sendToServer(new ServerboundFriendMoonUpdatePacket(
                                    clientBlockPos, MoonlightBasinBlockEntity.FriendMoonUpdatePacket.AWAKEN
                                ));
                            }
                        } else
                            setFriendMoonAnimation(FriendMoonAnimation.PHASES);
                    } else // Set default animation to emotion (server chosen)
                        setFriendMoonAnimation(EMOTION);
                }
            }

        }
        if (fadeOut) {
            friendMoonOpacity = Math.max(friendMoonOpacity - fadeSpeed, 0);
            if (friendMoonOpacity <= 0)
                animationProgress = 0;
        }
//        Vector3f cameraPitchYaw = euler(cameraEntity.getForward().toVector3f());
//
//        Vector3f moonPosition = moonViewMatrix.transformPosition(0f, MOON_DISTANCE, 0f, new Vector3f());
//        Vector3f moonPitchYaw = euler(moonPosition.sub(cameraEntity.getPosition(partialTick).toVector3f()).normalize());
//
//        float moonRadius = 180f * ((float) Math.PI / 180f);
//        Vector2f moonRotation = new Vector2f(
//            (cameraPitchYaw.x - moonPitchYaw.x),
//            (cameraPitchYaw.y - moonPitchYaw.y)
//        );
//        float distance = moonRotation.length();
//        if (distance < moonRadius) {
//            float t = Math.clamp(1f - (distance / moonRadius), 0f, 1f);
//            float strength = 1f;
//            moonRotation.mul(t * strength);
//
//            NoMansLand.LOGGER.info("in radius");
//
//            friendMoonYawAngle += moonRotation.x;
//            friendMoonPitchAngle += moonRotation.y;
//        }
    }

    private static void renderFriendMoonInternal(Tesselator tesselator, Matrix4f matrix4f1, FriendMoonAnimation moonAnimation, float opacity, int animationFrame) {
        float[] shaderColor = RenderSystem.getShaderColor();
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], opacity);

        RenderSystem.setShaderTexture(0, moonAnimation.getLocation());
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float[] uvPositions = moonAnimation.getUV(animationFrame);
        buffer.addVertex(matrix4f1, -MOON_SIZE, MOON_DISTANCE, MOON_SIZE).setUv(uvPositions[0], uvPositions[3]);
        buffer.addVertex(matrix4f1, MOON_SIZE, MOON_DISTANCE, MOON_SIZE).setUv(uvPositions[2], uvPositions[3]);
        buffer.addVertex(matrix4f1, MOON_SIZE, MOON_DISTANCE, -MOON_SIZE).setUv(uvPositions[2], uvPositions[1]);
        buffer.addVertex(matrix4f1, -MOON_SIZE, MOON_DISTANCE, -MOON_SIZE).setUv(uvPositions[0], uvPositions[1]);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], 1f);
    }

    public static void renderFriendMoon(Matrix4f frustumMatrix, Tesselator tesselator, PoseStack poseStack, float partialTick, int moonPhase) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();

        poseStack.mulPose(frustumMatrix);
        poseStack.pushPose();

        // Moon Rotation in the sky
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(friendMoonYawAngle));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(friendMoonPitchAngle));

        // Billboarding
        Matrix4f matrix4f1 = poseStack.last().pose();
        Matrix4f originalPose = new Matrix4f(matrix4f1);

        Entity cameraEntity = mc.getCameraEntity();

        Vector3f cameraForward = cameraEntity.getForward().toVector3f()
            .mul(new Vector3f(-1f, 0f, 1f)).normalize();

        Vector3f cameraUp = cameraEntity.getUpVector(partialTick).toVector3f()
            .mul(new Vector3f(-1f, 1f, 1f));

        if (Math.abs(cameraForward.dot(cameraUp)) > 0.999f)
            cameraUp = new Vector3f(0f, 0.1f, 0f);

        Vector3f right = new Vector3f(cameraUp).cross(new Vector3f(cameraForward).negate()).normalize();
        Vector3f up = new Vector3f(cameraForward).negate().cross(right);

        matrix4f1.mul(new Matrix4f(
            right.x, up.x, -cameraForward.x, 0f,
            right.y, up.y, -cameraForward.y, 0f,
            right.z, up.z, -cameraForward.z, 0f,
            0f, 0f, 0f, 1f
        ));

        // Update moon rotation / position
        updateFriendMoonPosition(cameraEntity, originalPose, partialTick);

        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        // Rendering moon
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        renderFriendMoonInternal(tesselator, matrix4f1, getFriendMoonAnimation(), getFriendMoonOpacity(), (int) animationProgress);

        RenderSystem.enableCull();

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        poseStack.popPose();
        RenderSystem.depthMask(true);
    }
}
