package com.farcr.nomansland.common.world.generation;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.HashMap;
import java.util.Map;

// dirty, dirty hacks !
// trying to do this without making a billion extra new objects
// and overwhelming the garbage collector.
public class LazyDensityFunctionVisitor implements DensityFunction.Visitor {
    private static final Map<Long, LazyDensityFunctionVisitor> visitorCache = new HashMap<>();

    public static LazyDensityFunctionVisitor getOrCreate(long worldSeed) {
        // i think this technically causes a memory leak (if you keep making and deleting hundreds of worlds with different seeds)
        // but it will never actually matter in practice.
        return visitorCache.computeIfAbsent(worldSeed, (seed) -> new LazyDensityFunctionVisitor(RandomSource.create(seed)));
    }

    private final RandomSource randomSource;
    private final Map<NormalNoise.NoiseParameters, DensityFunction.NoiseHolder> holderCache;

    public LazyDensityFunctionVisitor(RandomSource randomSource) {
        this.randomSource = randomSource;
        this.holderCache = new HashMap<>();
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        return densityFunction;
    }

    @Override
    public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
        NormalNoise.NoiseParameters noiseParameters = noiseHolder.noiseData().value();
        return this.holderCache.computeIfAbsent(noiseParameters,
                (params) -> new DensityFunction.NoiseHolder(noiseHolder.noiseData(), NormalNoise.create(this.randomSource, params))
        );
    }
}
