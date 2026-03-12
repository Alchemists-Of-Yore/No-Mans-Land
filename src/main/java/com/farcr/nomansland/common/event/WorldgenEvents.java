package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.handler.sanctuary_grid.SanctuaryCell;
import com.farcr.nomansland.common.handler.sanctuary_grid.SanctuaryGridHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class WorldgenEvents {

    @SubscribeEvent
    public static void onLevelTick(final LevelTickEvent.Pre preTick) {
        final Level level = preTick.getLevel();
        if (level instanceof final ServerLevel sl) {
            final LocalPlayer player = Minecraft.getInstance().player;

            if (player != null) {
                final ChunkPos chunkPos = player.chunkPosition();
                final SanctuaryCell cell = SanctuaryGridHandler.getCell(sl.getSeed(), chunkPos.getMinBlockX(), chunkPos.getMinBlockZ());

                if (cell != null) {
                    player.displayClientMessage(Component.literal("gridX " + cell.x)
                            .append(" gridZ " + cell.z)
                            .append(" valid " + cell.valid())
                            .append(cell.valid() ? " firstChunkPos " + cell.getFirstSanctuaryPos().toString() : "")
                            .append(cell.valid() ? " secondChunkPos " + cell.getSecondSanctuaryPos().toString() : ""), true);
                }
            }
        }
    }
}
