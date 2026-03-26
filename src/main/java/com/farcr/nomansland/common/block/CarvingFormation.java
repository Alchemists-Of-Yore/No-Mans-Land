package com.farcr.nomansland.common.block;

import net.minecraft.util.StringRepresentable;

public enum CarvingFormation implements StringRepresentable {
    SINGLE("single", 1, 0, 0),
    TWO_TOP_LEFT("two_top_left", 2, 0, 0),
    TWO_TOP_RIGHT("two_top_right", 2, 1, 0),
    TWO_BOTTOM_LEFT("two_bottom_left", 2, 0, 1),
    TWO_BOTTOM_RIGHT("two_bottom_right", 2, 1, 1),
    THREE_0_0("three_0_0", 3, 0, 0),
    THREE_1_0("three_1_0", 3, 1, 0),
    THREE_2_0("three_2_0", 3, 2, 0),
    THREE_0_1("three_0_1", 3, 0, 1),
    THREE_1_1("three_1_1", 3, 1, 1),
    THREE_2_1("three_2_1", 3, 2, 1),
    THREE_0_2("three_0_2", 3, 0, 2),
    THREE_1_2("three_1_2", 3, 1, 2),
    THREE_2_2("three_2_2", 3, 2, 2);

    private final String name;
    private final int size;
    private final int col;
    private final int row;

    CarvingFormation(String name, int size, int col, int row) {
        this.name = name;
        this.size = size;
        this.col = col;
        this.row = row;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public static CarvingFormation getForPosition(int size, int col, int row) {
        for (CarvingFormation f : values()) {
            if (f.size == size && f.col == col && f.row == row) return f;
        }
        return SINGLE;
    }
}
