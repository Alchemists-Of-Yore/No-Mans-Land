package com.farcr.nomansland.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;

public final class FriendMoonOfferingSoundStarter {
    private FriendMoonOfferingSoundStarter() {}

    public static Object start(BlockPos pos) {
        FriendMoonOfferingSound instance = new FriendMoonOfferingSound(pos);
        Minecraft.getInstance().getSoundManager().play(instance);
        return instance;
    }

    public static boolean isActive(Object instance) {
        return instance instanceof SoundInstance soundInstance
            && Minecraft.getInstance().getSoundManager().isActive(soundInstance);
    }
}
