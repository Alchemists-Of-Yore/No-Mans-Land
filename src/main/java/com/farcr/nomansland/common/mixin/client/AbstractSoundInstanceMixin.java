package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
        NoMansLand.LOGGER.info(" getting getting contextual volume : " + NML$contextualVolume);
        return NML$contextualVolume;
    }
}
