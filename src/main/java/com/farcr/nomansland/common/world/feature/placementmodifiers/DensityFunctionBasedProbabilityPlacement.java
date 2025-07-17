package com.farcr.nomansland.common.world.feature.placementmodifiers;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.worldgen.NMLPlacementModifiers;
import com.farcr.nomansland.common.world.generation.LazilyCachedDensityFunctionSeedifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.stream.Stream;

// "density_function_based_probability" placement type!
//
// variation of the density_function_based_count placement type,
// removing placement spots based off a noise value rather than
// adding new ones.
//
// EXAMPLE SYNTAX:
//   {
//      "type": "nomansland:density_function_based_probability",
//      "noise_function": "minecraft:overworld/base_3d_noise", // defaults to some reasonable noise.
//                                                                can be a path to any density function, or inlined.
//      "noise_scale": 1.0,         // defaults to 1.0.
//                                     a multiplier for the frequency of noise.
//      "minimum_probability": 0.0, // defaults to 0.0.
//                                     the minimum probability that a given position will be kept.
//      "maximum_probability": 1.0, // defaults to 1.0.
//                                     the maximum probability that a given position will be kept.
//      "normalize_noise": true // defaults to false. determines whether to rescale the noise to always fall between the minimum and maximum
//                                 probability, or simply to clamp it.
//   }
public class DensityFunctionBasedProbabilityPlacement extends PlacementModifier {
    private static final DensityFunction DEFAULT_DENSITY_FUNCTION =
            DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(-4, 1)));

    public static final MapCodec<DensityFunctionBasedProbabilityPlacement> CODEC = RecordCodecBuilder.mapCodec (
            codec -> codec.group(
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise_function").orElse(DEFAULT_DENSITY_FUNCTION).forGetter(instance -> instance.densityFunction),
                            Codec.DOUBLE.fieldOf("noise_scale").orElse(1.0).forGetter(instance -> instance.noiseScale),
                            Codec.DOUBLE.fieldOf("minimum_probability").orElse(0.0).forGetter(instance -> instance.minimumProbability),
                            Codec.DOUBLE.fieldOf("maximum_probability").orElse(1.0).forGetter(instance -> instance.maximumProbability),
                            Codec.BOOL.fieldOf("normalize_noise").orElse(false).forGetter(instance -> instance.normalizeNoise)
                    ).apply(codec, DensityFunctionBasedProbabilityPlacement::new)
    );

    private final DensityFunction densityFunction;
    private final double noiseScale;
    private final double minimumProbability;
    private final double maximumProbability;
    private final boolean normalizeNoise;

    public DensityFunctionBasedProbabilityPlacement(DensityFunction densityFunction, double noiseScale, double minimumProbability, double maximumProbability, boolean normalizeNoise) {
        this.densityFunction = densityFunction;
        this.noiseScale = noiseScale;
        this.minimumProbability = minimumProbability;
        this.maximumProbability = maximumProbability;
        this.normalizeNoise = normalizeNoise;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        // apply seeds to all the noise, if that hasn't been done yet
        // this operation is cached. i hate this nonetheless!
        DensityFunction seedifiedDensityFunction = densityFunction.mapAll(LazilyCachedDensityFunctionSeedifier.getOrCreate(context.getLevel()));
        // sample the noise, with the proper seed!
        double threshold = seedifiedDensityFunction.compute(
                new DensityFunction.SinglePointContext(
                        (int) (pos.getX() * this.noiseScale),
                        (int) (pos.getY() * this.noiseScale),
                        (int) (pos.getZ() * this.noiseScale)
                )
        );

        // uncomment this line to preview noise values!
        // if (random.nextInt(32) == 0) NoMansLand.LOGGER.info(threshold);

        if (normalizeNoise) {
            threshold = Mth.clampedMap(threshold, densityFunction.minValue(), densityFunction.maxValue(),  minimumProbability, maximumProbability);
        } else {
            threshold = Mth.clamp(threshold, minimumProbability, maximumProbability);
        }

        return random.nextDouble() < threshold ? Stream.of(pos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return NMLPlacementModifiers.DENSITY_FUNCTION_BASED_PROBABILITY.get();
    }
}
