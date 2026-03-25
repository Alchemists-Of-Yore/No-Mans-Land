package com.farcr.nomansland.common.extension;

public interface ClientChunkCacheExtension {
    default boolean nml$isInOverride(int x, int z) {
        return false;
    }

    default void nml$addToOverride(int x, int z) {}
}
