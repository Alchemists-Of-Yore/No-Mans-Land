package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class BellSanctuaryEvents {

    @SubscribeEvent
    public static void onLevelLoad(final LevelEvent.Load onLoad) {
        if (onLoad.getLevel() instanceof final ServerLevel sl) {
            BellSanctuaryGridHandler.populateOrCreateData(sl);
        }
    }

    @SubscribeEvent
    public static void onServerStop(final ServerStoppedEvent onStop) {
        BellSanctuaryGridHandler.clean();
    }
}