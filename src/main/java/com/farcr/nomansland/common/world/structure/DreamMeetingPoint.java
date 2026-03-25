package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class DreamMeetingPoint extends Structure {
    private final Holder<StructureTemplatePool> startPool;
    public static final MapCodec<DreamMeetingPoint> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool)
        ).apply(instance, DreamMeetingPoint::new)
    );

    public DreamMeetingPoint(Structure.StructureSettings settings, Holder<StructureTemplatePool> startPool) {
        super(settings);
        this.startPool = startPool;
    }

    private static final int offset = 6;
    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos center = new BlockPos(chunkPos.getMinBlockX() - offset, 0, chunkPos.getMinBlockZ() - offset);

        RandomSource random = context.random();
        StructurePoolElement element = startPool.value().getRandomTemplate(random);
        StructureTemplateManager templates = context.structureTemplateManager();

        return Optional.of(new GenerationStub(center, structurePiecesBuilder -> {
            // horrible way to do it but it works LOL
            for (int i = 0; i < (MoonlightDreamType.MONOLITH_HEIGHT + 2); i++) {
                PoolElementStructurePiece meetingPoint = new PoolElementStructurePiece(
                    templates, element, center.above(i), 0,
                    Rotation.NONE, element.getBoundingBox(templates, center, Rotation.NONE),
                    LiquidSettings.IGNORE_WATERLOGGING
                );
                structurePiecesBuilder.addPiece(meetingPoint);
            }
        }));
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.DREAM_MEETING_POINT.get();
    }
}
