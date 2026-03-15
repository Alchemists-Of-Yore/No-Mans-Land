package com.farcr.nomansland.common.world.structure.bell_sanctuary;

import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import com.farcr.nomansland.common.registry.worldgen.NMLStructurePlacements;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class BellSanctuaryStructurePlacement extends RandomSpreadStructurePlacement {

    public static final MapCodec<BellSanctuaryStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            p_204996_ -> placementCodec(p_204996_)
                    .and(
                            p_204996_.group(
                                    Codec.intRange(0, 4096).fieldOf("spacing").forGetter(RandomSpreadStructurePlacement::spacing),
                                    Codec.intRange(0, 4096).fieldOf("separation").forGetter(RandomSpreadStructurePlacement::separation),
                                    RandomSpreadType.CODEC
                                            .optionalFieldOf("spread_type", RandomSpreadType.LINEAR)
                                            .forGetter(RandomSpreadStructurePlacement::spreadType)
                            )
                    )
                    .apply(p_204996_, BellSanctuaryStructurePlacement::new)
    );

    public BellSanctuaryStructurePlacement(final Vec3i locateOffset, final FrequencyReductionMethod frequencyReductionMethod, final float frequency, final int salt, final Optional<ExclusionZone> exclusionZone, final int spacing, final int separation, final RandomSpreadType spreadType) {
        super(locateOffset, frequencyReductionMethod, frequency, salt, exclusionZone, spacing, separation, spreadType);
    }

    @Override
    protected boolean isPlacementChunk(final ChunkGeneratorStructureState chunkGeneratorStructureState, final int chunkX, final int chunkZ) {
        return BellSanctuaryGridHandler.tryGeneratePair(chunkGeneratorStructureState.getLevelSeed(), new ChunkPos(chunkX, chunkZ));
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
