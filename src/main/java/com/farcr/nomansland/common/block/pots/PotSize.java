package com.farcr.nomansland.common.block.pots;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum PotSize implements StringRepresentable {
    SMALL("small"),
    LARGE("large");

    private final String name;

    public static final Codec<PotSize> CODEC = StringRepresentable.fromEnum(PotSize::values);

    PotSize(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
