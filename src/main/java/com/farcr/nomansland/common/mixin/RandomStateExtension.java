package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.watershed.Watershed;

public interface RandomStateExtension {
    Watershed nml$getWatershed(int blockX, int blockZ);
}
