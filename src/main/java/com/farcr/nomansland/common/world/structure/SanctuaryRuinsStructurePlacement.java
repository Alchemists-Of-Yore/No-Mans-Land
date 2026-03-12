package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.handler.sanctuary_grid.SanctuaryCell;
import com.farcr.nomansland.common.handler.sanctuary_grid.SanctuaryGridHandler;
import com.farcr.nomansland.common.registry.worldgen.NMLStructurePlacements;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class SanctuaryRuinsStructurePlacement extends StructurePlacement {

    public static final MapCodec<SanctuaryRuinsStructurePlacement> MAP_CODEC = MapCodec.unit(SanctuaryRuinsStructurePlacement::new);

    protected SanctuaryRuinsStructurePlacement() {
        super(new Vec3i(8, 0, 8), FrequencyReductionMethod.DEFAULT, 1, 0, Optional.empty());
    }

    @Override
    protected boolean isPlacementChunk(final ChunkGeneratorStructureState chunkGeneratorStructureState, final int chunkX, final int chunkZ) {
        final SanctuaryCell cell = SanctuaryGridHandler.generateOrGetCell(chunkGeneratorStructureState, this, chunkX * 16, chunkZ * 16);
        if (!cell.isValid()) {
            return false;
        }

        return cell.validGenChunk(chunkX, chunkZ);
    }

    @Override
    public boolean isStructureChunk(final ChunkGeneratorStructureState structureState, final int x, final int z) {
        return this.isPlacementChunk(structureState, x, z);
    }

    @Override
    public StructurePlacementType<?> type() {
        return NMLStructurePlacements.SANCTUARY_RUINS.get();
    }
}
