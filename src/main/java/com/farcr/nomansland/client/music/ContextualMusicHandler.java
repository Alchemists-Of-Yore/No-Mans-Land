package com.farcr.nomansland.client.music;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.music.condition.MusicCondition;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundSource;

import java.util.ArrayList;
import java.util.List;

/* Heavily inspired by Cappin's Fog Modifiers (I love fog modifiers...) */
public class ContextualMusicHandler {
    public static boolean builtContext = false;
    public static List<MusicCondition.MusicConditionInstance> instanceList = new ArrayList<>();
    public static void buildMusicContext() {
        NMLRegistries.CONTEXTUAL_MUSIC.holders().forEach(
            (reference) -> instanceList.add(new MusicCondition.MusicConditionInstance(reference.value()))
        );
        builtContext = true;
    }

    // From modern versions of Minecraft
    public static boolean soundIsMusic(final SoundInstance soundInstance, final Music music) {
        return soundInstance != null && (music.getEvent().value()).getLocation().equals(soundInstance.getLocation());
    }

    public static void fadeSong(SoundInstance currentSong, SoundEngine soundEngine) {
        currentSong.NML$setContextualVolume(Math.max(currentSong.NML$getContextualVolume() - FADE_SPEED, 0f));
        NoMansLand.LOGGER.info(currentSong.NML$getContextualVolume());
        soundEngine.updateCategoryVolume(SoundSource.MUSIC,
            Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MUSIC)
        );
    }

    public static float FADE_SPEED = 0.01f;
    public static boolean tick(SoundInstance currentSong, MusicManager musicManager, SoundEngine soundEngine) {
        if (!builtContext)
            return false;

        for (MusicCondition.MusicConditionInstance conditionInstance : instanceList) {
            if (conditionInstance.canPlayMusic()) {
                if (!soundIsMusic(currentSong, conditionInstance.getMusic())) {
                    if (currentSong != null && currentSong.NML$getContextualVolume() > 0f)
                        fadeSong(currentSong, soundEngine);
                    else {
                        musicManager.stopPlaying();
                        musicManager.startPlaying(conditionInstance.getMusic());
                    }
                }
                // Cancel out of music ticks
                return true;
            } else if (soundIsMusic(currentSong, conditionInstance.getMusic())) {
                fadeSong(currentSong, soundEngine);
                if (currentSong.NML$getContextualVolume() <= 0f)
                    musicManager.stopPlaying();
                return true;
            }
        }
        return false;
    }
}
