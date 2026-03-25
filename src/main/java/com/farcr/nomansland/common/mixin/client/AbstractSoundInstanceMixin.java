package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractSoundInstance.class)
public abstract class AbstractSoundInstanceMixin implements SoundInstanceExtension {
    @Unique private float NML$contextualVolume = 1.0F;

    @Override
    public void NML$setContextualVolume(float newVolume) {
        this.NML$contextualVolume = newVolume;
    }

    @Override
    public float NML$getContextualVolume() {
        return NML$contextualVolume;
    }

    @Unique boolean nml$bypassDeafening = false;

    @Override
    public boolean nml$getBypassDeafening() {
        return this.nml$bypassDeafening;
    }

    @Override
    public void nml$setBypassDeafening(boolean newBypassDeafening) {
        this.nml$bypassDeafening = newBypassDeafening;
    }
}
