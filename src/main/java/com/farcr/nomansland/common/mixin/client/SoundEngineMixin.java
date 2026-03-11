package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class SoundEngineMixin {
    @Inject(method = "calculateVolume(Lnet/minecraft/client/resources/sounds/SoundInstance;)F", at = @At("RETURN"), cancellable = true)
    private void calculateVolume(SoundInstance sound, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValue() * ((SoundInstanceExtension) sound).NML$getContextualVolume());
    }
}
