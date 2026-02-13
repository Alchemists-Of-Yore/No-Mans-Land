package com.farcr.nomansland.client.music.condition;

import net.minecraft.sounds.Music;

import java.util.function.Supplier;

public abstract class MusicCondition {
    public Music getMusic() { return null; };
    public Supplier<Boolean> getCondition() { return null; }

    public static class MusicConditionInstance {
        public MusicConditionInstance(MusicCondition condition) {
            this.musicCondition = condition;
        }
        public MusicCondition musicCondition;
        public Music getMusic() { return musicCondition.getMusic(); }
        public boolean canPlayMusic() {
            return (musicCondition.getCondition() != null)
                && (musicCondition.getCondition().get());
        }
    }
}
