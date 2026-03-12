package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.handler.sanctuary_grid.SanctuaryGrid;
import com.farcr.nomansland.common.handler.sanctuary_grid.SanctuaryGridHandler;
import com.farcr.nomansland.common.registry.worldgen.NMLStructurePlacements;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class SanctuaryRuinsStructurePlacement extends StructurePlacement {

    public static final MapCodec<SanctuaryRuinsStructurePlacement> MAP_CODEC = MapCodec.unit(SanctuaryRuinsStructurePlacement::new);

    protected SanctuaryRuinsStructurePlacement() {
        super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1, 0, Optional.empty());
    }

    @Override
    protected boolean isPlacementChunk(final ChunkGeneratorStructureState chunkGeneratorStructureState, final int chunkX, final int chunkZ) {
        final SanctuaryGrid grid = SanctuaryGridHandler.getGrid(chunkGeneratorStructureState.getLevelSeed());
        grid.generateCellIfAbsent(chunkX * 16, chunkZ * 16);
        return false;
    }

    @Override
    public boolean isStructureChunk(final ChunkGeneratorStructureState structureState, final int x, final int z) {
        return super.isStructureChunk(structureState, x, z);
    }

    @Override
    public StructurePlacementType<?> type() {
        return NMLStructurePlacements.SANCTUARY_RUINS.get();
    }
}
