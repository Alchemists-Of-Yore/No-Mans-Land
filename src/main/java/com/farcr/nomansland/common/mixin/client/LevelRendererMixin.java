package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.UpperAtmosphericRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.block.pots.LargePotBlock;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

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

    @Inject(method = "graphicsChanged", at = @At("HEAD"))
    private void nml$enableStencilTarget(CallbackInfo ci) {
        FriendMoonRenderer.enableStencilTarget();
    }

    /*
    * TODO replace this !!!
    */
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
        ClientDreamRenderer manager = ClientDreamRenderer.getInstance();
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

    @Inject(
        method = "renderSky",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nml$renderLevel(
        Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci
    ) {
        ClientDreamRenderer clientManager = ClientDreamRenderer.getInstance();
        if (clientManager.dreamShouldRender() && clientManager.getRenderer() != null) {
            boolean cancelSkybox = clientManager.getRenderer().render(
                nml$Self, new PoseStack(),
                Minecraft.getInstance().getTimer(),
                frustumMatrix, projectionMatrix
            );
            if (cancelSkybox) ci.cancel();
        }
    }

    @Inject(
        method = "renderClouds",
        at = @At("HEAD"),
        cancellable = true
    )
    private void nml$renderClouds(
        PoseStack poseStack, Matrix4f frustumMatrix,
        Matrix4f projectionMatrix, float partialTick,
        double camX, double camY, double camZ, CallbackInfo ci
    ) {
        ClientDreamRenderer clientManager = ClientDreamRenderer.getInstance();
        if (clientManager.dreamShouldRender() && clientManager.getRenderer() != null) {
            if (!clientManager.getRenderer().shouldRenderClouds()) ci.cancel();
        }
    }

    /*
    * Renders post rain calc, unrelated but this happens before
    * moon rendering and post sky rendering so its good enough
    */
    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"
        )
    )
    private void nml$renderFriendMoonFog(
        Matrix4f frustumMatrix, Matrix4f projectionMatrix,
        float partialTick, Camera camera, boolean isFoggy,
        Runnable skyFogSetup, CallbackInfo ci
    ) {
        FriendMoonRenderer.getInstance().renderFriendMoonFog(
            frustumMatrix, projectionMatrix, new PoseStack()
        );
    }

    @Inject(
        method = "renderSky",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
            ordinal = 2,
            shift = At.Shift.AFTER
        )
    )
    private void nml$renderFriendMoon(
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
    private void nml$renderFinalize(
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
    private void nml$renderUpperAtmosphericSky(
            Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
            Camera camera, boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci,
            @Local PoseStack poseStack, @Local Vec3 skyColor) {
        UpperAtmosphericRenderer.INSTANCE.render(poseStack, projectionMatrix, (float) skyColor.x, (float) skyColor.y, (float) skyColor.z, partialTick);
    }

    @Inject(method = "destroyBlockProgress", at = @At("HEAD"), cancellable = true)
    private void nml$redirectUpperPotBreaking(int breakerId, BlockPos pos, int progress, CallbackInfo ci) {
        if (Minecraft.getInstance().level == null) return;
        BlockState state = Minecraft.getInstance().level.getBlockState(pos);
        if (state.getBlock() instanceof LargePotBlock pot && pot.isUpper(state)) {
            nml$Self.destroyBlockProgress(breakerId, pos.below(), progress);
            ci.cancel();
        }
    }
}
