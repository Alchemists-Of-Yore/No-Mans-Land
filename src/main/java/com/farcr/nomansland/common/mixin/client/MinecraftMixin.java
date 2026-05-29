package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private SoundManager soundManager;

    @Shadow
    public abstract DeltaTracker getTimer();

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;updateSource(Lnet/minecraft/client/Camera;)V"))
    private void deafenBell(boolean renderLevel, CallbackInfo ci) {
        float intensity = InvertedBellClientHandler.instance.getIntensity(this.getTimer().getRealtimeDeltaTicks());
        SoundEngineAccessor accessor = (SoundEngineAccessor) this.soundManager.soundEngine;
        if (intensity > 0) {
            accessor.getInstanceToChannel().forEach((instance, channel) -> {
                if (!((SoundInstanceExtension) instance).nml$getBypassDeafening()) {
                    float f = accessor.invokeCalculateVolume(instance) * (1 - intensity) * (1 - intensity);
                    channel.execute(sound -> {
                        sound.setVolume(f);
                    });
                }
            });
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void nml$OverrideSetScreenDream(Screen guiScreen, CallbackInfo ci) {
        ClientDreamRenderer renderer = ClientDreamRenderer.getInstance();
        if (renderer.clientIsDreaming() && renderer.dreamShouldRender()
        && ClientDreamRenderer.isBlacklistedScreen(guiScreen))
            ci.cancel();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void nml$skipBellDimensionScreen(Screen guiScreen, CallbackInfo ci) {
        if (InvertedBellClientHandler.instance.isActive()
                && (guiScreen instanceof ReceivingLevelScreen || guiScreen instanceof ProgressScreen)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "updateScreenAndTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;stop()V"))
    private void nml$keepSoundsThroughTransitions(SoundManager soundManager, Operation<Void> original) {
        if (InvertedBellClientHandler.instance.isActive()) return;
        ClientDreamRenderer renderer = ClientDreamRenderer.getInstance();
        if (renderer.clientIsDreaming()) return;
        original.call(soundManager);
    }
}
