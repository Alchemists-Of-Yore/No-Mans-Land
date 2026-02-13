package com.farcr.nomansland.common.world.blockstateproviders;

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

import java.util.List;

public class StrataStateProvider extends BlockStateProvider {
    public static final MapCodec<StrataStateProvider> CODEC = RecordCodecBuilder.mapCodec(
            codec -> codec.group(
                            BlockStateProvider.CODEC.fieldOf("above_state").forGetter(provider -> provider.above),
                            BlockStateProvider.CODEC.fieldOf("below_state").forGetter(provider -> provider.below),
                            Codec.INT.fieldOf("gradient_top_height").forGetter(provider -> provider.topHeight),
                            Codec.INT.fieldOf("gradient_end_height").forGetter(provider -> provider.bottomHeight)
                    ).apply(codec, (BlockStateProvider layers, BlockStateProvider randomize, Integer randomize2, Integer randomize3) -> new StrataStateProvider(layers, randomize, , randomize2))
    );

    final BlockStateProvider[] byHeight;
    final float noiseIntensity;
    final NormalNoise noise;

    StrataStateProvider(List<StrataLayer> layers, boolean randomize, float noiseIntensity) {
        this.noiseIntensity = noiseIntensity;
        int totalHeight = 0;
        for (StrataLayer layer : layers) totalHeight += layer.height();

        this.byHeight = new BlockStateProvider[totalHeight];
        int currentHeight = 0;

        RandomSource random = RandomSource.create(0);
        if (randomize) layers = Util.shuffledCopy(layers.toArray(new StrataLayer[0]), random);
        for (StrataLayer layer : layers) {
            for (int j = 0; j < layer.height(); j++) {
                this.byHeight[currentHeight++] = layer.state();
            }
        }
        this.noise = NormalNoise.create(random, -4, 1.0);
    }

    @Override
    public BlockState getState(RandomSource random, BlockPos pos) {
        int localY = (int) Math.floorDiv(
                Math.round(pos.getY() + noise.getValue(pos.getX(), pos.getY(), pos.getZ()) * noiseIntensity),
                byHeight.length
        );
        return byHeight[localY].getState(random, pos);
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return NMLBlockStateProviderTypes.STRATA.get();
    }

    record StrataLayer(BlockStateProvider state, int height) {
        public static final Codec<StrataLayer> CODEC = RecordCodecBuilder.create(
                codec -> codec.group(
                        BlockStateProvider.CODEC.fieldOf("state").forGetter(StrataLayer::state),
                        Codec.INT.fieldOf("height").forGetter(StrataLayer::height)
                ).apply(codec, StrataLayer::new)
        );
    }
}
