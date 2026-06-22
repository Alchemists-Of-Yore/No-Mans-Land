package com.farcr.nomansland.client.music.condition;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.Music;

import java.util.function.Supplier;

public class AnachronisticMusicCondition extends MusicCondition {
    private final static Music ANACHRONISTIC_SONG = new Music(NMLSounds.ANACHRONISTIC_MUSIC, 0, 0, true);

    @Override
    public Music getMusic() {
        return ANACHRONISTIC_SONG;
    }

    @Override
    public Supplier<Boolean> getCondition() {
        return () -> {
            final Minecraft minecraft = Minecraft.getInstance();
            final LocalPlayer player = minecraft.player;
            final ClientLevel level = minecraft.level;
            if (player == null || level == null) return false;
            return player.getY() > 100.0 && level.canSeeSky(player.blockPosition());
        };
    }
}
