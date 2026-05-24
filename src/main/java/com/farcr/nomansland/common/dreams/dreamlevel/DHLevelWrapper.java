package com.farcr.nomansland.common.dreams.dreamlevel;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.common.wrappers.world.ServerLevelWrapper_neoforge;
import com.seibel.distanthorizons.core.api.internal.SharedApi;
import com.seibel.distanthorizons.core.world.DhClientServerWorld;
import com.seibel.distanthorizons.core.world.DhServerWorld;
import net.minecraft.server.level.ServerLevel;

public class DHLevelWrapper {
    public static void getDHLevel(ServerLevel level) {
        if (!DhApi.Delayed.worldProxy.worldLoaded()) return;
        if (SharedApi.getAbstractDhWorld() instanceof DhServerWorld serverWorld)
            serverWorld.getOrLoadLevel(ServerLevelWrapper_neoforge.getWrapper(level));
        if (SharedApi.getAbstractDhWorld() instanceof DhClientServerWorld serverWorld)
            serverWorld.getOrLoadLevel(ServerLevelWrapper_neoforge.getWrapper(level));
    }
}
