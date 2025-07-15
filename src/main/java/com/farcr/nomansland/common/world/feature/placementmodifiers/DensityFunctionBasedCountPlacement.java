package com.farcr.nomansland.common.world.feature.placementmodifiers;

import com.farcr.nomansland.common.registry.worldgen.NMLPlacementModifiers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

// "density_function_based_count" placement type!
//
// variation of the noise_based_count placement type,
// taking in a density function rather than hardcoded
// noise values.
// this allows for more complicated curves, etc.
//
// EXAMPLE SYNTAX:
//   {
//      "type": "nomansland:density_function_based_count",
//      "noise_function": "minecraft:overworld/base_3d_noise" // defaults to some reasonable noise. can be a path to any density function, or inlined.
//      "noise_to_count_ratio": 5 // required - no default. describes the density of feature placements.
//      "noise_factor": 1.0 // defaults to 1.0. a multiplier for the frequency of noise.
//      "noise_offset": -0.25 // defaults to 0.0. adds a value to the noise!
//   }
public class DensityFunctionBasedCountPlacement extends RepeatingPlacement {
    private static final DensityFunction DEFAULT_DENSITY_FUNCTION =
            DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(-4, 1)));

    public static final MapCodec<DensityFunctionBasedCountPlacement> CODEC = RecordCodecBuilder.mapCodec (
            codec -> codec.group(
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise_function").orElse(DEFAULT_DENSITY_FUNCTION).forGetter(instance -> instance.densityFunction),
                            Codec.INT.fieldOf("noise_to_count_ratio").forGetter(instance -> instance.noiseToCountRatio),
                            Codec.DOUBLE.fieldOf("noise_scale").orElse(1.0).forGetter(instance -> instance.noiseScale),
                            Codec.DOUBLE.fieldOf("noise_offset").orElse(0.0).forGetter(instance -> instance.noiseOffset)
                    ).apply(codec, DensityFunctionBasedCountPlacement::new)
    );

    private final DensityFunction densityFunction;
    private final int noiseToCountRatio;
    private final double noiseScale;
    private final double noiseOffset;

    public DensityFunctionBasedCountPlacement(DensityFunction densityFunction, int noiseToCountRatio, double noiseScale, double noiseOffset) {
        this.densityFunction = densityFunction;
        this.noiseToCountRatio = noiseToCountRatio;
        this.noiseScale = noiseScale;
        this.noiseOffset = noiseOffset;
    }

    @Override
    protected int count(RandomSource random, BlockPos pos) {
        double noise = densityFunction.compute(
                new DensityFunction.SinglePointContext(
                        (int) ((double) pos.getX() * this.noiseScale),
                        pos.getY(),
                        (int) ((double) pos.getZ() * this.noiseScale)
                )
        );
        return (int) Math.ceil( (noise + this.noiseOffset) * this.noiseToCountRatio );
    }

    @Override
    public PlacementModifierType<?> type() {
        return NMLPlacementModifiers.DENSITY_FUNCTION_BASED_COUNT.get();
    }
}
