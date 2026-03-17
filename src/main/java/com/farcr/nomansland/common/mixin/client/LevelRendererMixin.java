package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.dreams.DreamLevelHandler;
import com.farcr.nomansland.client.renderer.friend.FriendDreamRenderer;
import com.farcr.nomansland.client.renderer.friend.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.UpperAtmosphericRenderer;
import com.farcr.nomansland.common.friend.dream.DreamManager;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexBuffer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Final
    private ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections;

    @Shadow
    @Final
    private SectionOcclusionGraph sectionOcclusionGraph;

    @ModifyArg(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", ordinal = 4), index = 0)
    private ParticleOptions tryTurnSpawnerFlameMalevolent(ParticleOptions particle) {
        if (NMLConfig.MALEVOLENT_SPAWNER.get()) {
            return NMLParticleTypes.MALEVOLENT_FLAME.get();
        }
        return particle;
    }

    @Unique private static boolean FRIEND_RENDER_CONTEXT = false;
    @Unique private static boolean DREAM_RENDER_CONTEXT = false;

    @Unique private LevelRenderer nml$Self = (LevelRenderer) (Object) this;

    @Inject(
        method = "renderLevel",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nml$overrideRenderLevel(
        DeltaTracker deltaTracker, boolean renderBlockOutline,
        Camera camera, GameRenderer gameRenderer, LightTexture lightTexture,
        Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci
    ) {
        DreamManager.Client manager = DreamManager.Client.getInstance();
        if (!DREAM_RENDER_CONTEXT && manager.dreamShouldRender()) {
            DREAM_RENDER_CONTEXT = true;
            nml$Self.renderLevel(
                deltaTracker, false,
                camera, gameRenderer, lightTexture,
                frustumMatrix, projectionMatrix
            );
            DREAM_RENDER_CONTEXT = false;
            ci.cancel();
        }
    }

//    @Inject(
//        method = "renderLevel",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V",
//            shift = At.Shift.BEFORE
//        ),
//        cancellable = true
//    )
//    private void nml$renderLevel(
//        DeltaTracker deltaTracker, boolean renderBlockOutline,
//        Camera camera, GameRenderer gameRenderer,
//        LightTexture lightTexture, Matrix4f frustumMatrix,
//        Matrix4f projectionMatrix, CallbackInfo ci
//    ) {
//        FriendDreamRenderer renderer = FriendDreamRenderer.getInstance();
//        if (renderer.shouldRenderDream()) {
//            RenderSystem.depthMask(false);
//
//            PoseStack poseStack = new PoseStack();
//            poseStack.mulPose(frustumMatrix);
//            renderer.applyRotation(poseStack);
//            poseStack.pushPose();
//
//            renderer.renderDream(
//                nml$Self, poseStack, deltaTracker, renderBlockOutline, camera,
//                gameRenderer, lightTexture, frustumMatrix, projectionMatrix
//            );
//
//            if (starBuffer == null)
//                createStars();
//            starBuffer.bind();
//            starBuffer.drawWithShader(poseStack.last().pose(),
//                projectionMatrix, GameRenderer.getPositionShader());
//            VertexBuffer.unbind();
//
//            renderer.renderMoon(poseStack, projectionMatrix);
//
//            this.renderBuffers.bufferSource().endLastBatch();
//            RenderSystem.applyModelViewMatrix();
//            RenderSystem.depthMask(true);
//            RenderSystem.disableBlend();
//            FogRenderer.setupNoFog();
//
//            ci.cancel();
//        }
//    }

    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            ordinal = 2,
            shift = At.Shift.AFTER
        )
    )
    private void renderFriendMoon(
        Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
        Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci
    ) {
        FRIEND_RENDER_CONTEXT = true;
        FriendMoonRenderer renderer = FriendMoonRenderer.getInstance();
        renderer.renderFriendMoon(frustumMatrix, projectionMatrix, Tesselator.getInstance(), new PoseStack(), partialTick);
        renderer.renderFriendShadow(frustumMatrix, projectionMatrix, Tesselator.getInstance(), new PoseStack(), partialTick);
    }

    @Inject(
        method = "renderSky",
        at = @At(value = "TAIL")
    )
    private void renderFinalize(
        Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
        Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci
    ) {
        if (FRIEND_RENDER_CONTEXT) {
            FriendMoonRenderer.renderFinalize(frustumMatrix, projectionMatrix, Tesselator.getInstance(), partialTick);
            FRIEND_RENDER_CONTEXT = false;
        }
    }

    @Inject(
            method = "renderSky",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/DimensionSpecialEffects;getSunriseColor(FF)[F"
            )
    )
    private void renderUpperAtmosphericSky(
            Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
            Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci,
            @Local PoseStack poseStack, @Local Vec3 skyColor) {
        UpperAtmosphericRenderer.INSTANCE.render(poseStack, projectionMatrix, (float) skyColor.x, (float) skyColor.y, (float) skyColor.z, partialTick);
    }
}
