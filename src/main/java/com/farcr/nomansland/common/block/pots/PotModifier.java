package com.farcr.nomansland.common.block.pots;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum PotModifier implements StringRepresentable {
    ALIVE("alive"),
    INFESTED("infested"),
    OOZING("oozing"),
    TRAPPED("trapped"),
    WAXED("waxed");

    private final String name;

    public static final Codec<PotModifier> CODEC = StringRepresentable.fromEnum(PotModifier::values);

    PotModifier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
