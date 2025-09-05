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

import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
     "density_function_based_count" placement type!

     variation of the noise_based_count placement type,
     taking in a density function rather than hardcoded
     noise values. this allows for more complicated curves, etc.
     the placement position is duplicated based off the noise value.

     EXAMPLE SYNTAX:
       {
          "type": "nomansland:density_function_based_count",
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
                                   the noise value that will be mapped to the minimum count
          "noise_maximum": 1.0, // decimal number. defaults to 1.0.
                                   the noise value that will be mapped to the maximum count
          "count_minimum": 0, // integer. defaults to 0.
                                         the value which noise_minimum is mapped to.
                                         is not necessarily the minimum possible count, if clamped is false.
          "count_maximum": 12, // integer. defaults to 8.
                                         the value which noise_maximum is mapped to.
                                         is not necessarily the maximum possible count, if clamped is false.
          "clamped": true // boolean. defaults to true.
                             determines whether to clamp the probability to the given range.
       }
*/
public class DensityFunctionBasedCountPlacement extends PlacementModifier {
    public static final MapCodec<DensityFunctionBasedCountPlacement> CODEC = RecordCodecBuilder.mapCodec (
            codec -> codec.group(
                            DensityFunction.HOLDER_HELPER_CODEC.fieldOf("noise_function").forGetter(instance -> instance.densityFunction),
                            Codec.DOUBLE.fieldOf("noise_scale").orElse(1.0).forGetter(instance -> instance.noiseScale),
                            Codec.DOUBLE.fieldOf("noise_minimum").orElse(0.0).forGetter(instance -> instance.noiseMinimum),
                            Codec.DOUBLE.fieldOf("noise_maximum").orElse(1.0).forGetter(instance -> instance.noiseMaximum),
                            Codec.INT.fieldOf("count_minimum").orElse(0).forGetter(instance -> instance.countMinimum),
                            Codec.INT.fieldOf("count_maximum").orElse(8).forGetter(instance -> instance.countMaximum),
                            Codec.BOOL.fieldOf("clamped").orElse(true).forGetter(instance -> instance.clamped)
                    ).apply(codec, DensityFunctionBasedCountPlacement::new)
    );

    private final DensityFunction densityFunction;
    private final double noiseScale;
    private final double noiseMinimum;
    private final double noiseMaximum;
    private final int countMinimum;
    private final int countMaximum;
    private final boolean clamped;

    public DensityFunctionBasedCountPlacement(DensityFunction densityFunction, double noiseScale, double noiseMinimum, double noiseMaximum, int countMinimum, int countMaximum, boolean clamped) {
        this.densityFunction = densityFunction;
        this.noiseScale = noiseScale;
        this.noiseMinimum = noiseMinimum;
        this.noiseMaximum = noiseMaximum;
        this.countMinimum = countMinimum;
        this.countMaximum = countMaximum;
        this.clamped = clamped;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        // apply seeds to all the noise, if that hasn't been done yet
        // this operation is cached. i hate this nonetheless!
        DensityFunction seedifiedDensityFunction = densityFunction.mapAll(LazilyCachedDensityFunctionSeedifier.getOrCreate(context.getLevel()));
        double noise = seedifiedDensityFunction.compute(
                new DensityFunction.SinglePointContext(
                                (int) ((double) pos.getX() * this.noiseScale),
                                (int) ((double) pos.getY() * this.noiseScale),
                                (int) ((double) pos.getZ() * this.noiseScale)
                        )
                );

        // uncomment this line to preview noise values!
        // if (random.nextInt(16) == 0) NoMansLand.LOGGER.info(noise);

        int count = (int) Math.ceil(clamped ?
                Mth.clampedMap(noise, noiseMinimum, noiseMaximum, countMinimum, countMaximum) :
                Mth.map(noise, noiseMinimum, noiseMaximum, countMinimum, countMaximum));
        return IntStream.range(0, count)
                .mapToObj(i -> pos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return NMLPlacementModifiers.DENSITY_FUNCTION_BASED_COUNT.get();
    }
}
