package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.HashMap;
import java.util.Map;

// applies seeds to density functions outside the RandomState class
// using dirty, dirty hacks !
// trying to do this without making a billion extra new objects
// and overwhelming the garbage collector.
public abstract class LazilyCachedDensityFunctionSeedifier implements DensityFunction.Visitor {
    private static final Map<WorldGeneratorEntry, LazilyCachedDensityFunctionSeedifier> visitorCache = new HashMap<>();

    public static DensityFunction.Visitor getOrCreate(WorldGenLevel worldGenLevel) {
        return visitorCache.computeIfAbsent(new WorldGeneratorEntry(worldGenLevel.getSeed(), worldGenLevel.dimensionType()), (level) -> {
            if (worldGenLevel.getChunkSource() instanceof ServerChunkCache chunkCache) {
                return new RandomStateBasedDensityFunctionSeedifier(chunkCache.randomState());
            } else {
                return new FallbackDensityFunctionSeedifier(RandomSource.create(worldGenLevel.getSeed()));
            }
        });
    }
    public static void clearCache() {
        visitorCache.clear();
    }

    private final Map<ResourceKey, DensityFunction.NoiseHolder> holderCache;

    public LazilyCachedDensityFunctionSeedifier() {
        this.holderCache = new HashMap<>();
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        return densityFunction;
    }

    @Override
    public DensityFunction.NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
        NormalNoise.NoiseParameters noiseParameters = noiseHolder.noiseData().value();
        return this.holderCache.computeIfAbsent(noiseHolder.noiseData().unwrapKey().orElseThrow(), (resourceKey) -> modifyNoiseHolder(noiseHolder, noiseParameters));
    }

    abstract DensityFunction.NoiseHolder modifyNoiseHolder(DensityFunction.NoiseHolder noiseHolder, NormalNoise.NoiseParameters noiseParameters);

    private static class RandomStateBasedDensityFunctionSeedifier extends LazilyCachedDensityFunctionSeedifier {
        private final RandomState randomState;

        private RandomStateBasedDensityFunctionSeedifier(RandomState randomState) {
            super();
            this.randomState = randomState;
        }

        @Override
        DensityFunction.NoiseHolder modifyNoiseHolder(DensityFunction.NoiseHolder noiseHolder, NormalNoise.NoiseParameters noiseParameters) {
            // this function should probably be much more complex to account for legacy random sources
            // (see: RandomState.NoiseWiringHelper, an implicit class in its constructor for some godforsaken reason...)
            // but this should be good enough for NML's purposes
            return new DensityFunction.NoiseHolder(noiseHolder.noiseData(), randomState.getOrCreateNoise(noiseHolder.noiseData().unwrapKey().orElseThrow()));
        }
    }

    // used if the world gen level uses a different chunk source, for some reason.
    // aka: Some Fuckery is Happening.
    // will still output usable density functions, but the noise values might not align to world features if they happen to be using the same noise.
    private static final class FallbackDensityFunctionSeedifier extends LazilyCachedDensityFunctionSeedifier {
        private final RandomSource randomSource;

        private FallbackDensityFunctionSeedifier(RandomSource randomSource) {
            super();
            this.randomSource = randomSource;
        }

        @Override
        DensityFunction.NoiseHolder modifyNoiseHolder(DensityFunction.NoiseHolder noiseHolder, NormalNoise.NoiseParameters noiseParameters) {
            return new DensityFunction.NoiseHolder(noiseHolder.noiseData(), NormalNoise.create(this.randomSource, noiseParameters));
        }
    }

    private record WorldGeneratorEntry(long seed, DimensionType dimensionType) {}
}
