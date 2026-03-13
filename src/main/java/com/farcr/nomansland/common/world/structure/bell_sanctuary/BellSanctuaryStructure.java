package com.farcr.nomansland.common.world.structure.bell_sanctuary;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;

public class BellSanctuaryStructure extends Structure {

    public static final MapCodec<BellSanctuaryStructure> MAP_CODEC = RecordCodecBuilder.mapCodec(i ->
            i.group(settingsCodec(i)).apply(i, BellSanctuaryStructure::new));

    protected BellSanctuaryStructure(final StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(final GenerationContext context) {
        final StructureTemplateManager templateManager = context.structureTemplateManager();

        final ChunkPos chunkpos = context.chunkPos();
        final int x = chunkpos.getMiddleBlockX();
        final int z = chunkpos.getMiddleBlockZ();
        final int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        final ResourceLocation resourceLocation = NoMansLand.location("bell_sanctuary/bell_sanctuary_center");
        final Optional<StructureTemplate> templateOptional = templateManager.get(resourceLocation);

        final Optional<GenerationStub> stub;
        if (templateOptional.isPresent()) {
            final StructureTemplate t = templateOptional.get();
            final Vec3i s = t.getSize();
            final BlockPos pos = new BlockPos(x - s.getX() / 2, y - 38, z - s.getZ() / 2);
            stub = Optional.of(new GenerationStub(pos, (b) ->
                    b.addPiece(new CenterBellPiece(templateManager, resourceLocation, new StructurePlaceSettings().setLiquidSettings(LiquidSettings.IGNORE_WATERLOGGING), pos))));
        } else {
            stub = Optional.empty();
        }

        return stub;
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.BELL_SANCTUARY.get();
    }
}
