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
    
}