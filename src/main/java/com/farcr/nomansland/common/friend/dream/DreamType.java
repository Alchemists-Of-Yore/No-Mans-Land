package com.farcr.nomansland.common.friend.dream;

import com.farcr.nomansland.client.music.condition.MusicCondition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/*
* Class that stores information about dream types`
*/
public class DreamType {
    public final @Nullable BiFunction<ServerPlayer, ServerLevel, Boolean> biconsumer;
    public DreamType(@Nullable BiFunction<ServerPlayer, ServerLevel, Boolean> condition) {
        this.biconsumer = condition;
    }

    public boolean canSprint = true;
    public DreamType setCanSprint(boolean canSprint) {
        this.canSprint = canSprint;
        return this;
    }

    private boolean hideHUD = true;
    public boolean hideHUD() {
        return hideHUD;
    }

    public DreamType setHUDHidden(boolean hudHidden) {
        this.hideHUD = hudHidden;
        return this;
    }

    public static class DreamTypeInstance {
        public DreamTypeInstance(DreamType dreamType) {
            this.dreamType = dreamType;
        }
        public DreamType dreamType;
    }
}
