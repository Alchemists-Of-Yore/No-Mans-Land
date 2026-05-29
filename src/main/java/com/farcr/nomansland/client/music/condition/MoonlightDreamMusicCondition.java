package com.farcr.nomansland.client.music.condition;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.sounds.Music;

import java.util.function.Supplier;

public class MoonlightDreamMusicCondition extends MusicCondition {
    private final static Music MOONLIGHT_DREAM_SONG = new Music(NMLSounds.SHROOMANIAC_TARNISHED, 0, 0, true);

    @Override
    public Music getMusic() {
        return MOONLIGHT_DREAM_SONG;
    }

    @Override
    public Supplier<Boolean> getCondition() {
        return () -> ClientDreamRenderer.getInstance().getDream() instanceof MoonlightDreamType;
    }
}
