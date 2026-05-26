package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import com.farcr.nomansland.common.item.AncestralOathSwordItem;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.DreamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;


@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private SoundManager soundManager;

    @Shadow
    public abstract DeltaTracker getTimer();

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    @Nullable
    public HitResult hitResult;

    @Inject(
        method = "startAttack",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;attack(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;)V"
        ),
        cancellable = true
    )
    private void nml$oathAttackDiscard(CallbackInfoReturnable<Boolean> cir) {
        assert this.player != null;
        ItemStack itemStack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemStack.is(NMLItems.ANCESTRAL_OATH_SWORD) && IClientItemExtensions.of(itemStack) instanceof AncestralOathSwordClientExtensions extensions) {
            assert this.hitResult != null;
            if (extensions.clientCancelAttack((AncestralOathSwordItem) itemStack.getItem(), ((EntityHitResult) this.hitResult).getEntity()))
                cir.setReturnValue(false);
        }
    }

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
}
