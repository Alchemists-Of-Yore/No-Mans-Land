package com.farcr.nomansland.common.extension;

public interface SoundInstanceExtension {
    // I don't want to mess around with this potentially breaking sounds since this is VERY intrusive
    default float NML$getContextualVolume() {
        return 1.0f;
    };
    default void NML$setContextualVolume(float newVolume) {};

    default boolean nml$getBypassDeafening() {
        return false;
    }
    default void nml$setBypassDeafening(boolean newBypassDeafening) {}
}
