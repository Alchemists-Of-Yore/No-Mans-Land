package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class SanctuaryGrid {

    public static final int SHIFT = 13; //2 ^ SHIFT == 8192

    private final Table<Integer, Integer, SanctuaryCell> grid;

    public SanctuaryGrid() {
        this.grid = HashBasedTable.create();
    }

    public SanctuaryCell getCell(final int blockX, final int blockZ) {
        return this.grid.get(blockX >> SHIFT, blockZ >> SHIFT);
    }

    public void generateCellIfAbsent(final int blockX, final int blockZ) {
        final int cellX = blockX >> SHIFT; // should keep sign
        final int cellZ = blockZ >> SHIFT; // should keep sign

        SanctuaryCell sanctuaryCell = this.grid.get(cellX, cellZ);
        if (sanctuaryCell == null) {
            sanctuaryCell = new SanctuaryCell(cellX, cellZ);
            this.grid.put(cellX, cellZ, new SanctuaryCell(cellX, cellZ));
        }

        if (sanctuaryCell.valid() && !sanctuaryCell.hasAttemptedToGenerate()) {
            final SanctuaryCell[][] adjacent = new SanctuaryCell[3][3];

            //-1 -> 1
            for (int adjX = -1; adjX < 2; adjX++) {
                for (int adjZ = -1; adjZ < 2; adjZ++) {
                    //center cell
                    if (adjX == 0 && adjZ == 0) {
                        continue;
                    }

                    //+1 to avoid negative indices inside array
                    adjacent[adjX + 1][adjZ + 1] = this.grid.get(cellX + adjX, cellZ + adjZ);
                }
            }

            sanctuaryCell.generatePositions(adjacent);
        }
    }

    public void clean() {
        this.grid.clear();
    }
}
