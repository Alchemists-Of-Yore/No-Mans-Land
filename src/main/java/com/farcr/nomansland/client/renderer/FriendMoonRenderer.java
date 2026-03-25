package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.FriendMoonState;
import com.farcr.nomansland.common.friend.FriendMoonUpdate;
import com.farcr.nomansland.client.renderer.context.MeetingPointRenderContext;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.friend.FriendMoonUpdatePacket;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.joml.*;
import org.lwjgl.opengl.GL11;

import java.lang.Math;
import java.util.Optional;
import java.util.function.IntFunction;

public class FriendMoonRenderer implements AutoCloseable {
    public static FriendMoonRenderer INSTANCE = new FriendMoonRenderer();
    public static FriendMoonRenderer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new FriendMoonRenderer();
        return INSTANCE;
    }

    public static void destroy() {
        if (INSTANCE == null)
            return;
        INSTANCE.close();
        INSTANCE = null;
    }

    @Override
    public void close() {}

    public enum FriendMoonAnimation {
        TALKING(0, "talking", 1, 2),
        SURPRISED(1, "surprised", 1, 1),
        HIDDEN(2, "hidden", 1, 1),
        HIDDEN_2(3, "hidden_unfocused", 1, 1),
        PHASES(4, "phases", 6, 1),
        DREAM(5, "dream", 1, 1),
        DREAM_UNFOCUSED(6, "dream_unfocused", 1, 1);

        public static final String textureLocation = "textures/misc/friendmoon_";
        private final ResourceLocation location;

        private final int xFrames;
        private final int yFrames;
        private final int id;

        FriendMoonAnimation(int id, String name, int xFrames, int yFrames) {
            this.location = NoMansLand.location(textureLocation + name + ".png");

            this.id = id;
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

    public float friendMoonOpacity = 0.0f;
    public float getFriendMoonOpacity() {
        return friendMoonOpacity;
    }
    public float friendShadowOpacity = 0.0f;

    public float badOmenWaitTime = 0.0f;
    public static final int MAX_BAD_OMEN_WAIT_TIME = 60;

    public static final float MOON_SIZE = 20f;
    public static final float MOON_DISTANCE = 50f;
    public static final float MOON_UV_TRIM = 0.375f;
    public static final float LOOKING_AT_THRESHOLD = 0.1f;

    public FriendMoonAnimation MOON_ANIMATION = FriendMoonAnimation.TALKING;
    public void setFriendMoonAnimation(FriendMoonAnimation newAnimation) {
        if (MOON_ANIMATION != newAnimation)
            animationProgress = 0;
        MOON_ANIMATION = newAnimation;
    }
    public FriendMoonAnimation getFriendMoonEmotion(FriendMoon friendMoon) {
        if (friendMoon.getState() == FriendMoonState.NEGATIVE
        || friendMoon.getState() == FriendMoonState.UPSET)
            return FriendMoonAnimation.SURPRISED;
        return FriendMoonAnimation.TALKING;
    }

    public float friendMoonPitchAngle = 0f;
    public float friendMoonYawAngle = 0f;
    public float friendMoonPitchDirection = 0f;
    public float friendMoonYawDirection = 0f;
    public float friendMoonPitchStep = 0;
    public float friendMoonYawStep = 0;

    public static boolean moonOnScreen(Minecraft mc, Matrix4f moonViewMatrix, Matrix4f projectionMatrix, float threshold) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vector3f worldPosition = moonViewMatrix.transformPosition(0f, MOON_DISTANCE, 0F, new Vector3f());
        Vector4f clip = new Vector4f(worldPosition, 1f).mul(projectionMatrix);
        return (clip.w > 0.0f)
            && ((Math.abs(clip.x / clip.w) <= threshold)
            && (Math.abs(clip.y / clip.w) <= threshold));
    }

    // Retrieve Animation Type Logic
    private FriendMoonAnimation getFriendMoonAnimation() {
        return MOON_ANIMATION;
    }

    public float animationProgress = 0;
    public float talkAnimationProgress = 0; // separated for smoothness in talking

    public static boolean friendMoonIsOccluded(Minecraft mc, RandomSource random, Entity cameraEntity, Quaternionf moonRotation) {
        // Raycast for the Moon
        Vec3 start = cameraEntity.getEyePosition();
        Vector3f worldPosition = new Vector3f(0f, MOON_DISTANCE, 0f).rotate(moonRotation);
        Vec3 end = start.add(new Vec3(worldPosition));

        BlockHitResult cast = mc.level.clip(
            new ClipContext(
                start, end,
                ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE,
                CollisionContext.empty()
            )
        );
        return cast.getType() == HitResult.Type.BLOCK;
    }

    public BlockPos clientBlockPos;
    public void updateFriendMoonPosition(
        Entity cameraEntity, Quaternionf moonRotation,
        Matrix4f moonViewMatrix, Matrix4f projectionMatrix, float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused())
            return;

        LocalPlayer player = mc.player;

        boolean fadeOut = true;
        float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
        float fadeSpeed = deltaTime / 50f;
        float turnAnimateSpeed = deltaTime / (15f);

        boolean isAwake = false;
        boolean moonIsVisible = moonOnScreen(mc, moonViewMatrix, projectionMatrix, 0.5f);
        if (clientBlockPos != null) {
            Optional<MoonlightBasinBlockEntity> optionalBasin = player.level().getBlockEntity(clientBlockPos, NMLBlockEntities.MOONLIGHT_BASIN.get());
            if (optionalBasin.isPresent() && (optionalBasin.get().clientMoon != null)) {
                FriendMoon friendMoonInstance = optionalBasin.get().clientMoon;
                boolean dontShowUp = friendMoonInstance.cannotObtainFriendship(player);
                if (!dontShowUp) badOmenWaitTime = 0;
                // after reset wait time, so that it doesnt accidentally clear
                if (dontShowUp) {
                    // Only show up if the player is within bounds of the basin
                    AABB aabb = new AABB(clientBlockPos).inflate(MoonlightBasinBlockEntity.FRIENDSHIP_MAX_RANGE);
                    if (!aabb.contains(player.position()))
                        dontShowUp = false;
                }
                // Can stare up at the moon and it'll show up
                if (player.hasEffect(NMLEffects.FRIENDSHIP) || (dontShowUp && (badOmenWaitTime < MAX_BAD_OMEN_WAIT_TIME))) {
                    isAwake = friendMoonInstance.isAwake();
                    if (moonIsVisible || isAwake) {
                        fadeOut = false;
                        friendMoonOpacity = Math.min(friendMoonOpacity + fadeSpeed, 1);
                        // moon awakening logic
                        if (!dontShowUp) {
                            if (!isAwake) {
                                // moon rotation
                                if (friendMoonOpacity >= 1) {
                                    animationProgress = Math.min(animationProgress + turnAnimateSpeed, getFriendMoonAnimation().getFrames());
                                    if (animationProgress >= getFriendMoonAnimation().getFrames())
                                        FriendMoonUpdatePacket.toServer(FriendMoonUpdate.ToServer.AWAKEN);
                                } else
                                    setFriendMoonAnimation(FriendMoonAnimation.PHASES);
                            } else {
                                // Set default animation to emotion (server chosen)
                                setFriendMoonAnimation(getFriendMoonEmotion(friendMoonInstance));
                                DialogueState currentState = DialogueRenderer.getCurrentState();
                                if (currentState != null) {
                                    float talkSpeed = 1 / 3f;
                                    if (currentState.canSpeakCurrently() && !currentState.doneTalking) {
                                        talkAnimationProgress = (talkAnimationProgress + (deltaTime * talkSpeed)) % (getFriendMoonAnimation().getFrames() + 1);
                                        animationProgress = talkAnimationProgress;
                                    } else {
                                        talkAnimationProgress = (float) Math.floor(talkAnimationProgress);
                                        animationProgress = 0;
                                    }
                                } else
                                    animationProgress = 0;
                            }
                        } else {
                            // not showing up takes precedent over everything else
                            animationProgress = 0;
                            setFriendMoonAnimation(FriendMoonAnimation.PHASES);
                            if (friendMoonOpacity >= 1) {
                                badOmenWaitTime += deltaTime;
                                if (badOmenWaitTime > MAX_BAD_OMEN_WAIT_TIME) {
                                    ClientboundDialoguePacket packet = ClientboundDialoguePacket.timedDialoguePacket(
                                        NoMansLand.location("nobody_came"),
                                        NMLRegistries.SPECIAL_DIALOGUE_KEY.location(),
                                        Optional.of(player.getUUID()),
                                        player.getRandom()
                                    );
                                    packet.applyPacket(player.level(), player);
                                    DialogueRenderer.getCurrentState()
                                        .setOverrideColor(DialogueUtil.NOBODY_CAME_TEXT_COLOR);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (fadeOut) {
            friendMoonOpacity = Math.max(friendMoonOpacity - fadeSpeed, 0);
            if (friendMoonOpacity <= 0)
                animationProgress = 0;
        }

        RandomSource random = cameraEntity.getRandom();
        float pitch = cameraEntity.getViewXRot(partialTick) + 90;
        float yaw = -cameraEntity.getViewYRot(partialTick);
        float speed = deltaTime / 30;
        if (!isAwake)
            speed /= 4f;

        float pitchClamp = 90;

        float minX = Math.min(pitch - 20, pitchClamp);
        float minY = yaw - 30;
        float maxX = pitch + 20;
        float maxY = yaw + 30;

        float desiredPitchStep = friendMoonPitchDirection;
        float desiredYawStep = friendMoonYawDirection;

        friendMoonPitchStep = Mth.lerp(speed, friendMoonPitchStep, desiredPitchStep);
        friendMoonYawStep = Mth.lerp(speed, friendMoonYawStep, desiredYawStep);

        float targetPitch = friendMoonPitchAngle + friendMoonPitchStep;
        float targetYaw = friendMoonYawAngle + friendMoonYawStep;

        if (targetPitch < minX || targetPitch > maxX) {
            friendMoonPitchDirection = friendMoonPitchDirection == 1 ? -1 : 1;
            if (random.nextFloat() < 0.1) friendMoonYawDirection = friendMoonYawDirection == 1 ? -1 : 1;
        }

        if (targetYaw < minY || targetYaw > maxY) {
            friendMoonYawDirection = friendMoonYawDirection == 1 ? -1 : 1;
            if (random.nextFloat() < 0.1) friendMoonPitchDirection = friendMoonPitchDirection == 1 ? -1 : 1;
        }

        boolean moonIsOccluded = false;
        if (moonIsVisible && friendMoonIsOccluded(mc, random, cameraEntity, moonRotation)) {
            moonIsOccluded = true;

            float escapeTime = 0.25f;
            float moonMaxSpeed = 25f;
            if (Math.abs(friendMoonPitchStep) < moonMaxSpeed)
                friendMoonPitchStep += (pitch - targetPitch) * escapeTime;
            if (Math.abs(friendMoonYawStep) < moonMaxSpeed)
                friendMoonYawStep += (yaw - targetYaw) * escapeTime;
            friendMoonPitchDirection = Math.signum(friendMoonPitchStep);
            friendMoonYawDirection = Math.signum(friendMoonYawStep);
        }

        friendMoonPitchAngle = Mth.lerp(speed, friendMoonPitchAngle, targetPitch);
        friendMoonYawAngle = Mth.lerp(speed, friendMoonYawAngle, targetYaw);

        if (!moonIsOccluded) {
            friendMoonPitchAngle = Mth.lerp(speed, friendMoonPitchAngle, Mth.clamp(friendMoonPitchAngle, minX, maxX));
            friendMoonYawAngle = Mth.lerp(speed, friendMoonYawAngle, Mth.clamp(friendMoonYawAngle, minY, maxY));
        }

        friendMoonPitchAngle = Math.min(friendMoonPitchAngle, pitchClamp - 25f);
    }

    public static void renderFriendMoonInternal(Tesselator tesselator, Matrix4f matrix4f1,
        FriendMoonAnimation moonAnimation, float opacity, int animationFrame, boolean trim
    ) {
        float[] shaderColor = RenderSystem.getShaderColor();
        float lastOpacity = shaderColor[3];
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], opacity);

        RenderSystem.setShaderTexture(0, moonAnimation.getLocation());
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float[] uvPositions = moonAnimation.getUV(animationFrame);
        float moonTrim = trim ? MOON_UV_TRIM : 0;
        float moonSize = MOON_SIZE * (1f - (moonTrim * 2f));
        buffer.addVertex(matrix4f1, -moonSize, MOON_DISTANCE, -moonSize).setUv(uvPositions[0] + moonTrim, uvPositions[3] + moonTrim);
        buffer.addVertex(matrix4f1, moonSize, MOON_DISTANCE, -moonSize).setUv(uvPositions[2] - moonTrim, uvPositions[3] + moonTrim);
        buffer.addVertex(matrix4f1, moonSize, MOON_DISTANCE, moonSize).setUv(uvPositions[2] - moonTrim, uvPositions[1] - moonTrim);
        buffer.addVertex(matrix4f1, -moonSize, MOON_DISTANCE, moonSize).setUv(uvPositions[0] + moonTrim, uvPositions[1] - moonTrim);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], lastOpacity);
    }

    public static float OFFSET_MAX_AMOUNT = 30f;
    public static float OFFSET_MAX_DISTANCE = 1000f;

    public static float NIGHT_TIME_THRESHOLD = 0.3f;

    public MeetingPointRenderContext meetingPointContext = MeetingPointRenderContext.fromDefault();
    public void renderFriendShadow(
        Matrix4f frustumMatrix, Matrix4f projectionMatrix,
        Tesselator tesselator, PoseStack poseStack, float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        assert player != null;
        if (meetingPointContext.enabled() && FriendMoon.displayFriendShadow(player)) {
            float time = player.level().getTimeOfDay(partialTick);
            boolean visible = (time > NIGHT_TIME_THRESHOLD && time < (1 - NIGHT_TIME_THRESHOLD));
            if (clientBlockPos != null) {
                AABB aabb = new AABB(clientBlockPos).inflate(MoonlightBasinBlockEntity.FRIENDSHIP_MAX_RANGE);
                if (aabb.contains(player.position()))
                    visible = false;
            }
            float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
            if (mc.isPaused())
                deltaTime = 0f;

            float moveSpeed = 0.25f;
            float t = (float) (1f - Math.exp(deltaTime * -moveSpeed));

            float moveTo = (visible ? 1 : 0);
            friendShadowOpacity = Mth.lerp(t, friendShadowOpacity, moveTo);
            if (friendShadowOpacity <= 0.1f)
                return;

            poseStack.mulPose(frustumMatrix);
            poseStack.pushPose();

            BlockPos blockPos = meetingPointContext.meetingPointPosition().subtract(meetingPointContext.originalLocation());
            float distanceMax = ((float) blockPos.distSqr(new Vec3i(0, 0, 0)) / (OFFSET_MAX_DISTANCE * OFFSET_MAX_DISTANCE));
            float offsetCalculation = Math.clamp(distanceMax, 0, 1) * OFFSET_MAX_AMOUNT;

            float degrees = (float) Math.toDegrees(Math.atan2(blockPos.getX(), blockPos.getZ()));
            degrees += offsetCalculation;

            poseStack.mulPose(Axis.YP.rotationDegrees(degrees));
            poseStack.mulPose(Axis.XP.rotationDegrees(65));

            RenderSystem.disableCull();
            RenderTarget target = mc.getMainRenderTarget();
            target.enableStencil();

            applyMultiplyBlendFunction();

            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

            Matrix4f moonViewMatrix = poseStack.last().pose();
            boolean moonIsVisible = moonOnScreen(mc, moonViewMatrix, projectionMatrix, LOOKING_AT_THRESHOLD);
            FriendMoonAnimation animation = moonIsVisible ? FriendMoonAnimation.HIDDEN : FriendMoonAnimation.HIDDEN_2;
            renderFriendMoonInternal(tesselator, moonViewMatrix, animation, friendShadowOpacity, 0, false);

            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
            RenderSystem.stencilOp(
                GL11.GL_KEEP,
                GL11.GL_KEEP,
                GL11.GL_REPLACE
            );

            RenderSystem.colorMask(false, false, false, false);
            renderFriendMoonInternal(tesselator, moonViewMatrix, animation, friendShadowOpacity, 0, true);
            RenderSystem.colorMask(true, true, true, true);

            RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
            RenderSystem.stencilOp(
                GL11.GL_KEEP,
                GL11.GL_KEEP,
                GL11.GL_KEEP
            );

            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();

            poseStack.popPose();
            return;
        }
        friendShadowOpacity = 0.0f;
    }

    // debug
    public static void debugLineRender(Vec3 start, Vec3 end) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();

        PoseStack pose = new PoseStack();
        Vec3 cam = camera.getPosition();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);

        Matrix4f tempPose = pose.last().pose();
        VertexConsumer vertexConsumer = mc.renderBuffers().bufferSource()
            .getBuffer(RenderType.lines());

        vertexConsumer.addVertex(tempPose,
                (float)(start.x),
                (float)(start.y),
                (float)(start.z))
            .setColor(255, 0, 0, 255)
            .setNormal(0, 1, 0);

        vertexConsumer.addVertex(tempPose,
                (float)(end.x),
                (float)(end.y),
                (float)(end.z))
            .setColor(255, 0, 0, 255)
            .setNormal(0, 1, 0);

        pose.popPose();
    }

    public static void applySkyBlendFunction() {
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    public static void applyMultiplyBlendFunction() {
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ZERO,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    /*
    * Stencil code will have to be rewritten eventually because I don't trust it but
    * for the most part it's fine and works as intended. eventually with vulkan I know I will have to make things more render agnostic
    * and not use GL calls but for the time being this will have to do
    */
    public void renderFriendMoon(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Tesselator tesselator, PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;

        poseStack.mulPose(frustumMatrix);
        poseStack.pushPose();

        // Moon Rotation in the sky
        Quaternionf rotationQuaternion = Axis.YP.rotationDegrees(friendMoonYawAngle)
                .mul(Axis.XP.rotationDegrees(friendMoonPitchAngle));
        poseStack.mulPose(rotationQuaternion);

        Matrix4f matrix4f1 = poseStack.last().pose();

        // Update moon rotation / position
        updateFriendMoonPosition(mc.getCameraEntity(), rotationQuaternion, matrix4f1, projectionMatrix, partialTick);
        if (getFriendMoonOpacity() <= 0)
            return;

        RenderSystem.enableBlend();
        RenderSystem.disableCull();

        RenderTarget target = mc.getMainRenderTarget();
        target.enableStencil();

        applySkyBlendFunction();

        // Render Full Moon
        renderFriendMoonInternal(tesselator, matrix4f1, getFriendMoonAnimation(), getFriendMoonOpacity(), (int) animationProgress, false);

        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
        RenderSystem.stencilOp(
            GL11.GL_KEEP,
            GL11.GL_KEEP,
            GL11.GL_REPLACE
        );

        // Rendering moon
        RenderSystem.colorMask(false, false, false, false);
        renderFriendMoonInternal(tesselator, matrix4f1, getFriendMoonAnimation(), getFriendMoonOpacity(), (int) animationProgress, true);
        RenderSystem.colorMask(true, true, true, true);

        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
        RenderSystem.stencilOp(
            GL11.GL_KEEP,
            GL11.GL_KEEP,
            GL11.GL_KEEP
        );

        RenderSystem.enableCull();
        poseStack.popPose();
    }

    public static void renderFinalize(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Tesselator tesselator, float partialTick) {
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}
