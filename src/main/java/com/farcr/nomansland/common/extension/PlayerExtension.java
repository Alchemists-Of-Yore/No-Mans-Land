package com.farcr.nomansland.common.extension;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.NotImplementedException;

public interface PlayerExtension {
    default BlockPos nml$getLastSleepPosition() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default ResourceKey<Level> nml$getLastSleepDimension() throws NotImplementedException {
        throw new NotImplementedException();
    }
}