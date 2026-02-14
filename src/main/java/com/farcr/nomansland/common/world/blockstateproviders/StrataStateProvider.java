package com.farcr.nomansland.common.world.blockstateproviders;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.worldgen.NMLBlockStateProviderTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.GeodeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StrataStateProvider extends BlockStateProvider {
    public static final MapCodec<StrataStateProvider> CODEC = RecordCodecBuilder.mapCodec(
            codec -> codec.group(
                            Codec.list(StrataLayer.CODEC).fieldOf("layers").forGetter(provider -> provider.layers),
                            Codec.FLOAT.fieldOf("noise_intensity").orElse(0.0F).forGetter(provider -> provider.noiseIntensity),
                            Codec.BOOL.fieldOf("randomize").orElse(false).forGetter(provider -> provider.randomize),
                            Codec.LONG.fieldOf("seed").orElse(0L).forGetter(provider -> provider.seed)
                    ).apply(codec, StrataStateProvider::new)
    );

    final List<StrataLayer> layers;
    final float noiseIntensity;
    final boolean randomize;
    final long seed;

    final BlockStateProvider[] byHeight;
    final NormalNoise noise;

    StrataStateProvider(List<StrataLayer> layers, float noiseIntensity, boolean randomize, long seed) {
        this.layers = Collections.unmodifiableList(layers);
        this.randomize = randomize;
        this.seed = seed;

        this.noiseIntensity = noiseIntensity;
        int totalHeight = 0;
        for (StrataLayer layer : layers) totalHeight += layer.height();

        this.byHeight = new BlockStateProvider[totalHeight];
        int currentHeight = 0;

        RandomSource random = RandomSource.create(this.seed);
        if (randomize) layers = Util.shuffledCopy(layers.toArray(new StrataLayer[0]), random);
        for (StrataLayer layer : layers) {
            for (int i = 0; i < layer.height(); i++) this.byHeight[currentHeight++] = layer.state();
        }
        this.noise = NormalNoise.create(random, -4, 1.0);
    }

    @Override
    public BlockState getState(RandomSource random, BlockPos pos) {
        double offset = 0;
        if (noiseIntensity != 0) offset = noise.getValue(pos.getX(), pos.getY(), pos.getZ()) * noiseIntensity;
        int localY = Math.floorMod(
                (int) Math.round(pos.getY() + offset),
                byHeight.length
        );
        return byHeight[localY].getState(random, pos);
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return NMLBlockStateProviderTypes.STRATA_STATE_PROVIDER.get();
    }

    record StrataLayer(BlockStateProvider state, int height) {
        public static final Codec<StrataLayer> CODEC = RecordCodecBuilder.create(
                codec -> codec.group(
                        BlockStateProvider.CODEC.fieldOf("data").forGetter(StrataLayer::state),
                        Codec.INT.fieldOf("height").forGetter(StrataLayer::height)
                ).apply(codec, StrataLayer::new)
        );
    }
}
