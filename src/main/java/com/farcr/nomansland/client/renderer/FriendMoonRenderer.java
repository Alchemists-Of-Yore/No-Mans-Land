package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.Meshes;
import com.farcr.nomansland.client.ambience.fogmodifiers.FriendMoonFogModifier;
import com.farcr.nomansland.client.renderer.dreams.MoonlightDreamRenderer;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.BuddyStar;
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
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FastColor;
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
import java.util.List;
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

    private float friendMoonOpacity = 0.0f;
    public float getFriendMoonOpacity() {
        return friendMoonOpacity;
    }
    private float friendShadowOpacity = 0.0f;
    private float friendShadowFaceOpacity = 0.0f;

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
            if (FriendMoon.appearConditionsMet(player, clientBlockPos) && optionalBasin.isPresent() && (optionalBasin.get().clientMoon != null)) {
                FriendMoon friendMoonInstance = optionalBasin.get().clientMoon;
                boolean dontShowUp = friendMoonInstance.cannotObtainFriendship(player);
                if (!dontShowUp) badOmenWaitTime = 0;

                // Can stare up at the moon and it'll show up
                if (!dontShowUp || (badOmenWaitTime < MAX_BAD_OMEN_WAIT_TIME)) {
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
        if (getFriendMoonOpacity() <= 0f)
            speed = 1;

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
    public static float OFFSET_MAX_DISTANCE = 2500f;
    public static float OFFSET_DIMINISH_DISTANCE = 500f;

    public MeetingPointRenderContext meetingPointContext = MeetingPointRenderContext.fromDefault();
    public void renderFriendShadow(
        Matrix4f frustumMatrix, Matrix4f projectionMatrix,
        Tesselator tesselator, PoseStack poseStack, float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        assert player != null;
        if (meetingPointContext.enabled()) {
            float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
            if (mc.isPaused()) deltaTime = 0f;

            poseStack.mulPose(frustumMatrix);
            poseStack.pushPose();

            BlockPos meetingPointPosition = meetingPointContext.meetingPointPosition();
            BlockPos blockPos = meetingPointPosition.subtract(meetingPointContext.originalLocation());
            float distanceMax = ((float) blockPos.distSqr(new Vec3i(0, 0, 0)) / (OFFSET_MAX_DISTANCE * OFFSET_MAX_DISTANCE));
            float offsetCalculation = Math.clamp(distanceMax, 0, 1) * OFFSET_MAX_AMOUNT;


            float xDist = (float) (meetingPointPosition.getCenter().x - player.position().x);
            float yDist = (float) (meetingPointPosition.getCenter().z - player.position().z);
            float actualDegrees = (float) Math.toDegrees(Math.atan2(xDist, yDist));
            float degrees = (float) Math.toDegrees(Math.atan2(blockPos.getX(), blockPos.getZ()));
            degrees += offsetCalculation;

            float finalDegrees = Mth.lerp(
                (float) Math.clamp(player.position().distanceToSqr(meetingPointPosition.getCenter())
                    / (OFFSET_DIMINISH_DISTANCE * OFFSET_DIMINISH_DISTANCE), 0, 1), actualDegrees, degrees
            );

            float totalDistance = (float) Math.sqrt(xDist * xDist + yDist * yDist);
            boolean visible = (FriendMoon.isNightTime(player.level())
                && !FriendMoon.appearConditionsMet(player, clientBlockPos));

            float moveSpeed = 0.2f;
            float t = (float) (1f - Math.exp(deltaTime * -moveSpeed));

            float moveTo = (visible ? 1 : 0);
            friendShadowOpacity = Mth.lerp(t, friendShadowOpacity, moveTo);
            float decreaseDistance = 32f;
            float compositeOpacity = Math.max(friendShadowOpacity - getFriendMoonOpacity()
                + Math.clamp((totalDistance - (decreaseDistance * 2)) / decreaseDistance, -1, 0), 0f);

            if (compositeOpacity <= 0.1f) return;

            poseStack.mulPose(Axis.YP.rotationDegrees(finalDegrees));
            poseStack.mulPose(Axis.XP.rotationDegrees(65));

            RenderSystem.disableCull();
            RenderTarget target = mc.getMainRenderTarget();
            target.enableStencil();

            applyMultiplyBlendFunction();

            Matrix4f moonViewMatrix = poseStack.last().pose();
            drawWithColor(FastColor.ARGB32.color(255, 255, 255), compositeOpacity, () -> {
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

                boolean moonIsVisible = moonOnScreen(mc, moonViewMatrix, projectionMatrix, LOOKING_AT_THRESHOLD);
                friendShadowFaceOpacity = Mth.lerp(t, friendShadowFaceOpacity, (moonIsVisible ? 1 : 0));

                renderFriendMoonInternal(tesselator, moonViewMatrix, FriendMoonAnimation.HIDDEN_2, compositeOpacity, 0, false);

                applySkyBlendFunction();
                renderFriendMoonInternal(tesselator, moonViewMatrix, FriendMoonAnimation.HIDDEN, friendShadowFaceOpacity, 0, false);

                applyMultiplyBlendFunction();

                RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
                RenderSystem.stencilOp(
                    GL11.GL_KEEP,
                    GL11.GL_KEEP,
                    GL11.GL_REPLACE
                );

                RenderSystem.colorMask(false, false, false, false);
                renderFriendMoonInternal(tesselator, moonViewMatrix, FriendMoonAnimation.HIDDEN_2, compositeOpacity, 0, false);
                RenderSystem.colorMask(true, true, true, true);

                RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
                RenderSystem.stencilOp(
                    GL11.GL_KEEP,
                    GL11.GL_KEEP,
                    GL11.GL_KEEP
                );
            }, true);

            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();

            poseStack.popPose();
            return;
        }
        friendShadowOpacity = 0.0f;
        friendShadowFaceOpacity = 0.0f;
    }

    public static void applySkyBlendFunction() {
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    public static void applyMultiplyBlendFunction() {
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
    }

    private VertexBuffer skyMesh;
    private VertexBuffer getSkyMesh() {
        if (skyMesh == null)
            skyMesh = createSkyMesh();
        skyMesh.bind();
        return skyMesh;
    }

    public static VertexBuffer createSkyMesh() {
        VertexBuffer skyMesh = new VertexBuffer(VertexBuffer.Usage.STATIC);
        skyMesh.bind();
        skyMesh.upload(Meshes.texturelessHemisphere(Tesselator.getInstance(),
            24, 24, Mth.PI * 0.55F, 1,
            1F, 1F, 1F, 0F,
            0.3F, 0.3F, 0.4F, 1F,
            0.2F, -0.25F));
        VertexBuffer.unbind();
        return skyMesh;
    }

    public static void drawWithColor(int color, float alpha, Runnable runnable, boolean composite) {
        float c0 = RenderSystem.getShaderColor()[0];
        float c1 = RenderSystem.getShaderColor()[1];
        float c2 = RenderSystem.getShaderColor()[2];
        float c3 = RenderSystem.getShaderColor()[3];

        float compositeAlpha = composite ? alpha : 1;
        RenderSystem.setShaderColor(
            ((float) FastColor.ARGB32.red(color) / 255f) * compositeAlpha,
            ((float) FastColor.ARGB32.green(color) / 255f) * compositeAlpha,
            ((float) FastColor.ARGB32.blue(color) / 255f) * compositeAlpha,
            alpha
        );

        runnable.run();

        RenderSystem.setShaderColor(
            c0, c1, c2, c3
        );
    }

    float elapsedTime = 0f;
    public static int getGradientColor() {
        return FastColor.ARGB32.color(
            175, 220, 135
        );
    }

    private FriendMoonFogModifier fogModifier = new FriendMoonFogModifier();
    private float fogOpacity = 0.0f;

    /*
    * Stencil code will have to be rewritten eventually because I don't
    * trust it but for the most part it's fine and works as intended.
    * eventually with vulkan I know I will have to make things more render
    * agnostic and not use GL calls but for the time being this will have to do
    */
    public void renderFriendMoon(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Tesselator tesselator, PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;

        poseStack.mulPose(frustumMatrix);

        // Renders separate from opacity
        assert mc.player != null;
        boolean enabledFog = FriendMoon.appearConditionsMet(mc.player, clientBlockPos);
        float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
        if (mc.isPaused()) deltaTime = 0.0f;
        float t = (float) (1f - Math.exp(deltaTime * -.2f));
        fogOpacity = Mth.lerp(t, fogOpacity, (enabledFog ? (0.5f + (getFriendMoonOpacity() / 2f)) : 0));

        if (fogOpacity > 0.01f) {
            applySkyBlendFunction();

            poseStack.pushPose();
            // Render Sky Fog prior to skybox as well
            int fogColor = FastColor.ARGB32.colorFromFloat(
                1f,
                fogModifier.getFogRedMultiplier(),
                fogModifier.getFogGreenMultiplier(),
                fogModifier.getFogBlueMultiplier()
            );
            applyMultiplyBlendFunction();
            drawWithColor(fogColor, fogOpacity, () -> {
                poseStack.scale(100f, 100f, 100f);
                MoonlightDreamRenderer.GRADIENT_SHADER.safeGetUniform("Slice").set(0.0f);
                getSkyMesh().drawWithShader(
                    poseStack.last().pose(), projectionMatrix,
                    MoonlightDreamRenderer.GRADIENT_SHADER
                );

                poseStack.mulPose(Axis.XP.rotationDegrees(180));
                getSkyMesh().drawWithShader(
                    poseStack.last().pose(), projectionMatrix,
                    MoonlightDreamRenderer.GRADIENT_SHADER
                );

                VertexBuffer.unbind();
            }, true);
            poseStack.popPose();
        }

        // Moon Rotation in the sky
        Quaternionf rotationQuaternion = Axis.YP.rotationDegrees(friendMoonYawAngle)
            .mul(Axis.XP.rotationDegrees(friendMoonPitchAngle));
        poseStack.mulPose(rotationQuaternion);

        Matrix4f matrix4f1 = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.enableCull();
        applySkyBlendFunction();

        // Update moon rotation / position
        updateFriendMoonPosition(mc.getCameraEntity(), rotationQuaternion, matrix4f1, projectionMatrix, partialTick);
        if (getFriendMoonOpacity() <= 0)
            return;

        RenderSystem.disableCull();

        // Render Friend Moon Afterwards
        poseStack.pushPose();

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

        renderBuddyStars(tesselator, matrix4f1, getFriendMoonOpacity());

        RenderSystem.enableCull();
        poseStack.popPose();
    }

    private static final ResourceLocation BUDDY_STAR_TEXTURE = NoMansLand.location("textures/misc/buddy_star.png");
    public static final float STAR_SIZE = 1.0f;

    public void renderBuddyStars(Tesselator tesselator, Matrix4f moonMatrix, float opacity) {
        if (clientBlockPos == null || opacity <= 0)
            return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null)
            return;

        Optional<MoonlightBasinBlockEntity> optionalBasin = player.level().getBlockEntity(clientBlockPos, NMLBlockEntities.MOONLIGHT_BASIN.get());
        if (optionalBasin.isEmpty() || optionalBasin.get().clientMoon == null)
            return;

        List<BuddyStar> stars = optionalBasin.get().clientMoon.getBuddyStars();
        if (stars.isEmpty())
            return;

        RenderSystem.setShaderTexture(0, BUDDY_STAR_TEXTURE);

        long timeMs = System.currentTimeMillis();

        float[] prevColor = RenderSystem.getShaderColor();
        for (int i = 0; i < stars.size(); i++) {
            BuddyStar star = stars.get(i);
            float angleRad = (float) Math.toRadians(star.getAngle());
            float dist = star.getDistance(i);

            float x = (float) (Math.sin(angleRad)) * dist;
            float y = MOON_DISTANCE;
            float z = (float) (Math.cos(angleRad)) * dist;

            float[] rgb = star.getRgb();
            float flicker = star.getFlickerAlpha(timeMs);
            RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], opacity * flicker);

            BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.addVertex(moonMatrix, x - STAR_SIZE, y, z - STAR_SIZE).setUv(0, 1);
            buffer.addVertex(moonMatrix, x + STAR_SIZE, y, z - STAR_SIZE).setUv(1, 1);
            buffer.addVertex(moonMatrix, x + STAR_SIZE, y, z + STAR_SIZE).setUv(1, 0);
            buffer.addVertex(moonMatrix, x - STAR_SIZE, y, z + STAR_SIZE).setUv(0, 0);
            BufferUploader.drawWithShader(buffer.buildOrThrow());
        }
        RenderSystem.setShaderColor(prevColor[0], prevColor[1], prevColor[2], prevColor[3]);
    }

    public static void renderFinalize(Matrix4f frustumMatrix, Matrix4f projectionMatrix, Tesselator tesselator, float partialTick) {
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}
