package com.farcr.nomansland.common.world.structure.bell_sanctuary;

import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.farcr.nomansland.common.registry.worldgen.NMLStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class CenterBellPiece extends TemplateStructurePiece {

    public CenterBellPiece(final StructureTemplateManager structureManager, final CompoundTag tag) {
        super(NMLStructurePieces.CENTRAL_BELL_SANCTUARY_PIECE.get(), tag, structureManager, rl -> new StructurePlaceSettings());
    }

    public CenterBellPiece(final StructureTemplateManager templateManager, final ResourceLocation location, final StructurePlaceSettings structurePlaceSettings, final BlockPos pos) {
        super(NMLStructurePieces.CENTRAL_BELL_SANCTUARY_PIECE.get(), 0, templateManager, location, location.toString(), structurePlaceSettings, pos);
    }

    @Override
    protected void handleDataMarker(final String name, final BlockPos pos, final ServerLevelAccessor level, final RandomSource random, final BoundingBox box) {
        if (name.startsWith("inverted_bell_")) {
            final String suffix = name.substring("inverted_bell_".length());
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
                if (level.getBlockEntity(pos.above()) instanceof final InvertedBellBlockEntity ibbe) {
                    ibbe.state = InvertedBellBlockEntity.PositionState.UNASSIGNED;
                }
            }
        }
    }
}
