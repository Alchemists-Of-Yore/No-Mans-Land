package com.farcr.nomansland.common.world.structure;

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
        final int y = context.chunkGenerator().getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        final Optional<StructureTemplate> templateOptional = templateManager.get(NoMansLand.location("bell_sanctuary/bell_sanctuaries_1"));

        final Optional<GenerationStub> stub;
        if (templateOptional.isPresent()) {
            final StructureTemplate t = templateOptional.get();
            final Vec3i s = t.getSize();
            final BlockPos pos = new BlockPos(x - s.getX() / 2, y + 1, z - s.getZ() / 2);
            stub = Optional.of(new GenerationStub(pos, (b) ->
                    b.addPiece(new SingleBellSanctuaryPiece(context.chunkGenerator().getGenDepth(), templateManager, NoMansLand.location("bell_sanctuary/bell_sanctuaries_1"), new StructurePlaceSettings(), pos))));
        } else {
            stub = Optional.empty();
        }

        return stub;
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.BELL_SANCTUARY.get();
    }

    public static class SingleBellSanctuaryPiece extends TemplateStructurePiece {

        public SingleBellSanctuaryPiece(final int genDepth, final StructureTemplateManager structureTemplateManager, final ResourceLocation location, final StructurePlaceSettings placeSettings, final BlockPos templatePosition) {
            super(StructurePieceType.JIGSAW, genDepth, structureTemplateManager, location, location.toString(), placeSettings, templatePosition);
        }

        @Override
        protected void handleDataMarker(final String name, final BlockPos pos, final ServerLevelAccessor level, final RandomSource random, final BoundingBox box) {
            if (name.startsWith("inverted_bell_")) {
                String suffix = name.substring("inverted_bell_".length());
                Direction dir = switch (suffix) {
                    case "north" -> Direction.NORTH;
                    case "south" -> Direction.SOUTH;
                    case "east" -> Direction.EAST;
                    case "west" -> Direction.WEST;
                    default -> null;
                };
                if (dir != null) {
                    dir = this.getRotation().rotate(dir);
                    InvertedBellBlock.placeBell(pos, dir, level);
                    if (level.getBlockEntity(pos.above()) instanceof InvertedBellBlockEntity ibbe) {
                        ibbe.state = InvertedBellBlockEntity.PositionState.UNASSIGNED;
                    }
                }
            }
        }
    }
}
