package com.farcr.nomansland.client.music.condition;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.sounds.Music;

import java.util.function.Supplier;

public class GuidanceMusicCondition extends MusicCondition {
    private final static Music GUIDANCE_SONG = new Music(NMLSounds.MUSIC_DISC_GUIDANCE, 0, 0, true);

    @Override
    public Music getMusic() {
        return GUIDANCE_SONG;
    }

    @Override
    public Supplier<Boolean> getCondition() {
        return () -> (FriendMoonRenderer.getInstance().getFriendMoonOpacity() > 0);
    }
}
