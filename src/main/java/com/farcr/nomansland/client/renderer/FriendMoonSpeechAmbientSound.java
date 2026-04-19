package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class FriendMoonSpeechAmbientSound extends AbstractTickableSoundInstance {
    public FriendMoonSpeechAmbientSound() {
        super(NMLSounds.FRIEND_MOON_SPEAK_AMBIENT_LOOP.get(), SoundSource.AMBIENT, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.3f;
        this.pitch = 1.0f;
        this.relative = true;
        this.attenuation = Attenuation.NONE;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        var state = DialogueRenderer.getCurrentState();
        if (state == null || state.doneTalking || state.isPaused())
            this.stop();
    }
}
