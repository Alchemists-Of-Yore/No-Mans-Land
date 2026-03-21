package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void nml$getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> info) {
        if (source.is(NMLDamageTypes.SPIKE_POKE)) info.setReturnValue(NMLSounds.PLAYER_HURT_SPIKE_TRAP.get());
    }

    @Unique private Player nml$Self = (Player) (Object) this;

    @Inject(method = "stopSleepInBed", at = @At("HEAD"), cancellable = true)
    private void nml$stopSleepingInBed(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        if (DreamManager.isDreamingPlayer(nml$Self, true))
            ci.cancel();
    }

    @ModifyExpressionValue(
        method = "updatePlayerPose",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;isSleeping()Z"
        )
    )
    private boolean nml$modifySleepingPose(boolean original) {
        if (nml$Self.level().isClientSide && DreamManager.isDreamingPlayer(nml$Self, true))
            return !DreamManager.Client.getInstance().dreamShouldRender();
        return original;
    }

    @Inject(method = "isImmobile", at = @At("RETURN"), cancellable = true)
    private void nml$ReplaceIsImmobile(CallbackInfoReturnable<Boolean> cir) {
        if (nml$Self.level().isClientSide && DreamManager.isDreamingPlayer(nml$Self, true))
            cir.setReturnValue(!DreamManager.Client.getInstance().dreamShouldRender());
    }
}