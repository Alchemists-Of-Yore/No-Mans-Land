package com.farcr.nomansland.common.mixin.bell_sanctuary;

import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGrid;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import com.farcr.nomansland.common.world.structure.bell_sanctuary.BellSanctuaryStructurePlacement;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;

/**
 * Fix for {@link ChunkGenerator#findNearestMapStructure(ServerLevel, HolderSet, BlockPos, int, boolean)} not taking into account {@link com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryCell cells}
 */
@Mixin(ChunkGenerator.class)
public class FindNearestMapStructureFix {

    @Inject(method = "findNearestMapStructure", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getKey()Ljava/lang/Object;", ordinal = 0))
    private void nomansland$SanctuaryRuinsFinder(final ServerLevel level,
                                                 final HolderSet<Structure> structure,
                                                 final BlockPos pos,
                                                 final int searchRadius,
                                                 final boolean skipKnownStructures,
                                                 final CallbackInfoReturnable<Pair<BlockPos, Holder<Structure>>> cir,
                                                 @Local(name = "pair2") final LocalRef<Pair<BlockPos, Holder<Structure>>> localPair,
                                                 @Local(name = "entry") final Map.Entry<StructurePlacement, Set<Holder<Structure>>> localEntry) {
        final StructurePlacement placement = localEntry.getKey();
//        if (placement instanceof final BellSanctuaryStructurePlacement sanctPlacement) {
//            for (final Holder<Structure> iterStructure : localEntry.getValue()) {
//                final BellSanctuaryGrid grid = BellSanctuaryGridHandler.getGrid(level.getSeed());
//                final ChunkPos closest = grid.getClosestBellSanctuary(pos);
//
//                if (closest != null) {
//                    localPair.set(new Pair<>(closest.getBlockAt(8, pos.getY(), 8), iterStructure));
//                    break;
//                }
//            }
//        }
    }
}
