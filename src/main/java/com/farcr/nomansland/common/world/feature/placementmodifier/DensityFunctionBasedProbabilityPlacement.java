package com.farcr.nomansland.common.world.feature.placementmodifier;

import com.farcr.nomansland.common.registry.worldgen.NMLPlacementModifiers;
import com.farcr.nomansland.common.world.densityfunction.LazilyCachedDensityFunctionSeedifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/*
     "density_function_based_probability" placement type!

     variation of the density_function_based_count placement type,
     removing placement spots based off a noise value rather than
     adding new ones.
     the sampled noise value is remapped to the probability range,
     then used as a % chance that a given position is KEPT.

     EXAMPLE SYNTAX:
       {
          "type": "nomansland:density_function_based_probability",
          "noise_function": "minecraft:overworld/base_3d_noise", // noise router.
                                                                    can be a path to any density function, or inlined.
          "noise_scale": 1.0,         // decimal number. defaults to 1.0.
                                         a multiplier for the frequency of noise.
          // the following parameters are for remapping noise range.
          // useful for rescaling vanilla functions to fit a desired appearance!
          // the default values make it so no rescaling takes place.
          //     IF YOU DON'T QUITE GET RESCALING - look up the 'Map Range' function.
          //         a good reference: https://processing.org/reference/map_.html
          "noise_minimum": 0.0, // decimal number. defaults to 0.0.
                                   the noise value that will be mapped to the minimum probability
          "noise_maximum": 1.0, // decimal number. defaults to 1.0.
                                   the noise value that will be mapped to the maximum probability
          "probability_minimum": 0.0, // decimal number. defaults to 0.0.
                                         the value which noise_minimum is mapped to.
                                         is not necessarily the minimum possible probability, if clamped is false.
          "probability_maximum": 1.0, // decimal number. defaults to 1.0.
                                         the value which noise_maximum is mapped to.
                                         is not necessarily the maximum possible probability, if clamped is false.
          "clamped": true // boolean. defaults to true.
                             determines whether to clamp the probability to the given range.
       }
*/
public class DensityFunctionBasedProbabilityPlacement extends PlacementModifier {
    public static final MapCodec<DensityFunctionBasedProbabilityPlacement> CODEC = RecordCodecBuilder.mapCodec (
            codec -> codec.group(
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise_function").forGetter(instance -> instance.densityFunction),
                            Codec.DOUBLE.fieldOf("noise_scale").orElse(1.0).forGetter(instance -> instance.noiseScale),
                            Codec.DOUBLE.fieldOf("noise_minimum").orElse(0.0).forGetter(instance -> instance.noiseMinimum),
                            Codec.DOUBLE.fieldOf("noise_maximum").orElse(1.0).forGetter(instance -> instance.noiseMaximum),
                            Codec.DOUBLE.fieldOf("probability_minimum").orElse(0.0).forGetter(instance -> instance.probabilityMinimum),
                            Codec.DOUBLE.fieldOf("probability_maximum").orElse(1.0).forGetter(instance -> instance.probabilityMaximum),
                            Codec.BOOL.fieldOf("clamped").orElse(true).forGetter(instance -> instance.clamped)
            ).apply(codec, DensityFunctionBasedProbabilityPlacement::new)
    );

    private final DensityFunction densityFunction;
    private final double noiseScale;
    private final double noiseMinimum;
    private final double noiseMaximum;
    private final double probabilityMinimum;
    private final double probabilityMaximum;
    private final boolean clamped;

    public DensityFunctionBasedProbabilityPlacement(DensityFunction densityFunction, double noiseScale, double noiseMinimum, double noiseMaximum, double probabilityMinimum, double probabilityMaximum, boolean clamped) {
        this.densityFunction = densityFunction;
        this.noiseScale = noiseScale;
        this.noiseMinimum = noiseMinimum;
        this.noiseMaximum = noiseMaximum;
        this.probabilityMinimum = probabilityMinimum;
        this.probabilityMaximum = probabilityMaximum;
        this.clamped = clamped;
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

        threshold = clamped ?
                Mth.clampedMap(threshold, noiseMinimum, noiseMaximum, probabilityMinimum, probabilityMaximum) :
                       Mth.map(threshold, noiseMinimum, noiseMaximum, probabilityMinimum, probabilityMaximum);
        return random.nextDouble() < threshold ? Stream.of(pos) : Stream.empty();
    }

    @Override
    public PlacementModifierType<?> type() {
        return NMLPlacementModifiers.DENSITY_FUNCTION_BASED_PROBABILITY.get();
    }
}
