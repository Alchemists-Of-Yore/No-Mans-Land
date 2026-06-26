package com.farcr.nomansland.common.entity;

import net.minecraft.util.ByIdMap;

import java.util.function.IntFunction;

public enum BuriedVariant {
    ZERO(0),
    ONE(1),
    TWO(2);

    public static final IntFunction<BuriedVariant> BY_ID = ByIdMap.continuous(BuriedVariant::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    private final int id;

    BuriedVariant(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static BuriedVariant byId(int id) {
        return BY_ID.apply(id);
    }
}
