package com.farcr.nomansland.common.extension;

import java.util.List;

public interface BeardifierExtension {
    record AltarBeard(int centerX, int centerZ, int targetY, double radius) {}

    default void nomansland$setAltarBeards(List<AltarBeard> beards) {
    }

    default List<AltarBeard> nomansland$altarBeards() {
        return List.of();
    }
}
