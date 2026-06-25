package com.farcr.nomansland.common.world.structure.cave;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.ArrayList;
import java.util.List;

public final class Occupancy {
    private static final int CELL_SHIFT = 3;
    private final List<BoundingBox> rooms;
    private final BoundingBox total;
    private final LongOpenHashSet cells = new LongOpenHashSet();

    public Occupancy(PiecesContainer pieces) {
        List<BoundingBox> list = new ArrayList<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (StructurePiece piece : pieces.pieces()) {
            BoundingBox bounds = piece.getBoundingBox();
            list.add(bounds);
            minX = Math.min(minX, bounds.minX());
            minY = Math.min(minY, bounds.minY());
            minZ = Math.min(minZ, bounds.minZ());
            maxX = Math.max(maxX, bounds.maxX());
            maxY = Math.max(maxY, bounds.maxY());
            maxZ = Math.max(maxZ, bounds.maxZ());
            for (int cellX = bounds.minX() >> CELL_SHIFT; cellX <= bounds.maxX() >> CELL_SHIFT; cellX++) {
                for (int cellY = bounds.minY() >> CELL_SHIFT; cellY <= bounds.maxY() >> CELL_SHIFT; cellY++) {
                    for (int cellZ = bounds.minZ() >> CELL_SHIFT; cellZ <= bounds.maxZ() >> CELL_SHIFT; cellZ++) {
                        this.cells.add(BlockPos.asLong(cellX, cellY, cellZ));
                    }
                }
            }
        }
        this.rooms = list;
        this.total = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean contains(BlockPos position) {
        if (!this.total.isInside(position)) return false;
        long key = BlockPos.asLong(position.getX() >> CELL_SHIFT, position.getY() >> CELL_SHIFT, position.getZ() >> CELL_SHIFT);
        if (!this.cells.contains(key)) return false;
        for (BoundingBox bounds : this.rooms) {
            if (bounds.isInside(position)) return true;
        }
        return false;
    }

    public List<BoundingBox> rooms() {
        return this.rooms;
    }

    public BoundingBox total() {
        return this.total;
    }
}
