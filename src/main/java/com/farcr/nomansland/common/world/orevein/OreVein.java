package com.farcr.nomansland.common.world.orevein;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public record OreVein(int spacing, int separation, float probability,
                      IntProvider radius, HeightProvider height,
                      OreConfiguration.TargetBlockState filler,
                      OreConfiguration.TargetBlockState ore,
                      OreConfiguration.TargetBlockState raw) {
    public static final Codec<OreVein> CODEC = RecordCodecBuilder.create(
            codec -> codec.group(
                    Codec.INT.fieldOf("spacing").validate(
                            (spacing) -> {
                                if (spacing % 16 != 0) return DataResult.error(() -> "Spacing must be multiple of 16!");
                                if (spacing <= 0) return DataResult.error(() -> "Spacing must be positive!");
                                return DataResult.success(spacing);
                            }
                    ).forGetter(OreVein::spacing),
                    Codec.INT.fieldOf("separation").forGetter(OreVein::separation),
                    Codec.FLOAT.fieldOf("probability").forGetter(OreVein::probability),
                    IntProvider.CODEC.fieldOf("radius").forGetter(OreVein::radius),
                    HeightProvider.CODEC.fieldOf("height").forGetter(OreVein::height),
                    OreConfiguration.TargetBlockState.CODEC.fieldOf("filler").forGetter(OreVein::filler),
                    OreConfiguration.TargetBlockState.CODEC.fieldOf("ore").forGetter(OreVein::ore),
                    OreConfiguration.TargetBlockState.CODEC.fieldOf("raw").forGetter(OreVein::raw)
            ).apply(codec, OreVein::new)
    );
}
