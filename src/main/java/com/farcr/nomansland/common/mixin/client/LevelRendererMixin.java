package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Pose;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyArg(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", ordinal = 4), index = 0)
    private ParticleOptions tryTurnSpawnerFlameMalevolent(ParticleOptions particle) {
        if (NMLConfig.MALEVOLENT_SPAWNER.get()) {
            return NMLParticleTypes.MALEVOLENT_FLAME.get();
        }
        return particle;
    }

    @Shadow
    private ClientLevel level;

    @Unique
    private static boolean FRIEND_RENDER_CONTEXT = false;

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
//        // Breakout Condition add more stuff here later
//        if (this.level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL)
//            return;
        FRIEND_RENDER_CONTEXT = true;
        FriendMoonRenderer.renderFriendShadow(frustumMatrix, projectionMatrix, Tesselator.getInstance(), new PoseStack(), partialTick);
        FriendMoonRenderer.renderFriendMoon(frustumMatrix, projectionMatrix, Tesselator.getInstance(), new PoseStack(), partialTick);
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
}
