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

    /*
    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Pre preTick) {
        final Level level = preTick.getLevel();
        if (level instanceof final ServerLevel sl) {
            final LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                final ChunkPos chunkPos = player.chunkPosition();
                final BellSanctuaryCell cell = BellSanctuaryGridHandler.getCell(sl.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());

                if (cell != null) {
                    player.displayClientMessage(Component.literal("gridX " + cell.x)
                            .append(" gridZ " + cell.z)
                            .append(" valid " + cell.isValid())
                            .append(cell.isValid() ? " firstChunkPos " + cell.getFirstBellSanctuaryPos().toString() : "")
                            .append(cell.isValid() ? " secondChunkPos " + cell.getSecondBellSanctuaryPos().toString() : ""), true);
                }
            }
        }
    }
     */

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