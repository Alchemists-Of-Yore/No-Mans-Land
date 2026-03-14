package com.farcr.nomansland.common.world.structure.bell_sanctuary;

import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryCell;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import com.farcr.nomansland.common.registry.worldgen.NMLStructurePlacements;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class BellSanctuaryStructurePlacement extends StructurePlacement {

    public static final MapCodec<BellSanctuaryStructurePlacement> MAP_CODEC = MapCodec.unit(BellSanctuaryStructurePlacement::new);

    protected BellSanctuaryStructurePlacement() {
        super(new Vec3i(8, 0, 8), FrequencyReductionMethod.DEFAULT, 1, 0, Optional.empty());
    }

    @Override
    protected boolean isPlacementChunk(final ChunkGeneratorStructureState chunkGeneratorStructureState, final int chunkX, final int chunkZ) {
        final BellSanctuaryCell cell = BellSanctuaryGridHandler.getCell(chunkGeneratorStructureState.getLevelSeed(), chunkX * 16, chunkZ * 16);
        if (cell == null || !cell.isValid()) {
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
        return NMLStructurePlacements.BELL_SANCTUARY.get();
    }
}
