package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import net.minecraft.client.resources.sounds.SoundInstance;
import org.spongepowered.asm.mixin.Mixin;

/*
* Importantly, the SoundInstance extension has to be applied
* to the sound instance, not the abstract sound instance
* in case anything inherits SoundInstance directly and not its abstract
*/
@Mixin(SoundInstance.class)
public interface SoundInstanceMixin extends SoundInstanceExtension {}
