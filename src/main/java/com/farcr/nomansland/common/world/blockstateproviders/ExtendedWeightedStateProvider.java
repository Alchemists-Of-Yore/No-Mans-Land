package com.farcr.nomansland.common.world.blockstateproviders;

import com.farcr.nomansland.common.registry.worldgen.NMLBlockStateProviderTypes;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class ExtendedWeightedStateProvider extends BlockStateProvider {
    public static final MapCodec<ExtendedWeightedStateProvider> CODEC =
            SimpleWeightedRandomList.wrappedCodec(BlockStateProvider.CODEC)
            .comapFlatMap(ExtendedWeightedStateProvider::create, provider -> provider.weightedList)
            .fieldOf("entries");
    private final SimpleWeightedRandomList<BlockStateProvider> weightedList;

    private static DataResult<ExtendedWeightedStateProvider> create(SimpleWeightedRandomList<BlockStateProvider> weightedList) {
        return weightedList.isEmpty() ? DataResult.error(() -> "ExtendedWeightedStateProvider with no states") : DataResult.success(new ExtendedWeightedStateProvider(weightedList));
    }

    ExtendedWeightedStateProvider(SimpleWeightedRandomList<BlockStateProvider> weightedList) {
        this.weightedList = weightedList;
    }

    @Override
    public BlockState getState(RandomSource random, BlockPos pos) {
        return weightedList.getRandomValue(random).get().getState(random, pos);
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return NMLBlockStateProviderTypes.EXTENDED_WEIGHTED_STATE_PROVIDER.get();
    }
}
