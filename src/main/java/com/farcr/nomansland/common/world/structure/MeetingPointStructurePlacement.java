package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.registry.worldgen.NMLStructurePlacements;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

import java.util.Optional;

public class MeetingPointStructurePlacement extends StructurePlacement {
    public static final MapCodec<MeetingPointStructurePlacement> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("preferred_biomes").forGetter(m -> m.preferredBiomes)
            ).apply(instance, MeetingPointStructurePlacement::new)
    );

    public final HolderSet<Biome> preferredBiomes;

    public MeetingPointStructurePlacement(HolderSet<Biome> preferredBiomes) {
        super(Vec3i.ZERO, StructurePlacement.FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty());
        this.preferredBiomes = preferredBiomes;
    }

    @Override
    protected boolean isPlacementChunk(ChunkGeneratorStructureState state, int x, int z) {
        ChunkPos meetingPoint = state.meetingPointPosition();
        return meetingPoint != null && meetingPoint.x == x && meetingPoint.z == z;
    }

    @Override
    public StructurePlacementType<?> type() {
        return NMLStructurePlacements.MEETING_POINT.get();
    }
}
