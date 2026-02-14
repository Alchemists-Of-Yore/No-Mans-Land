package com.farcr.nomansland.common.world.blockstateproviders;

import com.farcr.nomansland.common.registry.worldgen.NMLBlockStateProviderTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class VerticalGradientStateProvider extends BlockStateProvider {
    public static final MapCodec<VerticalGradientStateProvider> CODEC = RecordCodecBuilder.mapCodec(
            codec -> codec.group(
                            BlockStateProvider.CODEC.fieldOf("above_state").forGetter(provider -> provider.above),
                            BlockStateProvider.CODEC.fieldOf("below_state").forGetter(provider -> provider.below),
                            Codec.INT.fieldOf("gradient_top_height").forGetter(provider -> provider.topHeight),
                            Codec.INT.fieldOf("gradient_bottom_height").forGetter(provider -> provider.bottomHeight)
                    ).apply(codec, VerticalGradientStateProvider::new)
    );

    final BlockStateProvider above, below;
    final int topHeight, bottomHeight;
    final float difference;

    public VerticalGradientStateProvider(BlockStateProvider above, BlockStateProvider below, int topHeight, int bottomHeight) {
        this.above = above;
        this.below = below;
        this.topHeight = topHeight;
        this.bottomHeight = bottomHeight;
        this.difference = 1.0f / Math.abs(this.topHeight - this.bottomHeight);
    }

    @Override
    public BlockState getState(RandomSource random, BlockPos pos) {
        float yFactor = (pos.getY() - bottomHeight) * difference;
        if (random.nextFloat() < yFactor)
            return above.getState(random, pos);
        else
            return below.getState(random, pos);
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return NMLBlockStateProviderTypes.VERTICAL_GRADIENT_STATE_PROVIDER.get();
    }
}
