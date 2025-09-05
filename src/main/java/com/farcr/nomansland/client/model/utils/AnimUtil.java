package com.farcr.nomansland.client.model.utils;

import net.minecraft.util.Mth;

public class AnimUtil {
    // returns a sine wave with a period of one
    public static float wave(float time) {
        return Mth.sin(time * Mth.TWO_PI);
    }
}
