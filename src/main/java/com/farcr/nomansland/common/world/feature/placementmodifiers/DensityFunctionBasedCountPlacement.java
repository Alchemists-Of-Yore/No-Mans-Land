package com.farcr.nomansland.common.world.feature.placementmodifiers;

import com.farcr.nomansland.common.registry.worldgen.NMLPlacementModifiers;
import com.farcr.nomansland.common.world.generation.LazyDensityFunctionVisitor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.placement.RepeatingPlacement;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.stream.IntStream;
import java.util.stream.Stream;

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
//      "noise_function": "minecraft:overworld/base_3d_noise", // defaults to some reasonable noise.
//                                                                can be a path to any density function, or inlined.
//      "noise_to_count_ratio": 5, // required - no default.
//                                    describes the density of feature placements.
//      "noise_factor": 1.0, // defaults to 1.0.
//                              a multiplier for the frequency of noise.
//      "noise_offset": -0.25 // defaults to 0.0.
//                               adds a value to the noise!
//   }
public class DensityFunctionBasedCountPlacement extends PlacementModifier {
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
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        // apply seeds to all the noise, if that hasn't been done yet
        // this operation is cached. i hate this nonetheless!
        double noise = densityFunction.mapAll(LazyDensityFunctionVisitor.getOrCreate(context.getLevel().getSeed()))
                .compute(new DensityFunction.SinglePointContext(
                                (int) ((double) pos.getX() * this.noiseScale),
                                (int) ((double) pos.getY() * this.noiseScale),
                                (int) ((double) pos.getZ() * this.noiseScale)
                        )
                );
        int count = (int) Math.ceil((noise + this.noiseOffset) * this.noiseToCountRatio);
        return IntStream.range(0, count)
                .mapToObj(i -> pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return NMLPlacementModifiers.DENSITY_FUNCTION_BASED_COUNT.get();
    }
}
