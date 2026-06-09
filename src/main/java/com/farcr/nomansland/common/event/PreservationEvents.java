package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.saved_data.PreservedStructureData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.PistonEvent;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class PreservationEvents {

    @SubscribeEvent
    public static void onLevelLoad(final LevelEvent.Load event) {
        if (event.getLevel() instanceof final ServerLevel serverLevel) {
            PreservedStructureData.prime(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(final LevelEvent.Unload event) {
        if (event.getLevel() instanceof final ServerLevel serverLevel) {
            PreservedStructureData.drop(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(final BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof final ServerLevel serverLevel
                && PreservedStructureData.get(serverLevel).isPreserved(event.getPos().asLong())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(final ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof final ServerLevel serverLevel) {
            final PreservedStructureData data = PreservedStructureData.get(serverLevel);
            event.getAffectedBlocks().removeIf(pos -> data.isPreserved(pos.asLong()));
        }
    }

    @SubscribeEvent
    public static void onPiston(final PistonEvent.Pre event) {
        if (!(event.getLevel() instanceof final ServerLevel serverLevel)) return;

        final PreservedStructureData data = PreservedStructureData.get(serverLevel);
        final PistonStructureResolver resolver = event.getStructureHelper();

        if (resolver != null && resolver.resolve()) {
            for (final BlockPos pos : resolver.getToPush()) {
                if (data.isPreserved(pos.asLong())) {
                    event.setCanceled(true);
                    return;
                }
            }
            for (final BlockPos pos : resolver.getToDestroy()) {
                if (data.isPreserved(pos.asLong())) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }
}
