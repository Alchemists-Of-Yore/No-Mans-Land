package com.farcr.nomansland.common.block.pots;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum PotTrait implements StringRepresentable {
    REGENERATES("regenerates"),
    DROPS_EXPERIENCE("drops_experience"),
    FLAMMABLE("flammable"),
    BRITTLE("brittle"),
    LIVING("living");

    private final String name;

    public static final Codec<PotTrait> CODEC = StringRepresentable.fromEnum(PotTrait::values);

    PotTrait(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
