package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.blockstateproviders.ExtendedWeightedStateProvider;
import com.farcr.nomansland.common.world.blockstateproviders.StrataStateProvider;
import com.farcr.nomansland.common.world.blockstateproviders.VerticalGradientStateProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLBlockStateProviderTypes {
    public static final DeferredRegister<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, NoMansLand.MODID);

    public static final Supplier<BlockStateProviderType<VerticalGradientStateProvider>> VERTICAL_GRADIENT_STATE_PROVIDER =
            register("vertical_gradient_state_provider", VerticalGradientStateProvider.CODEC);
    public static final Supplier<BlockStateProviderType<StrataStateProvider>> STRATA_STATE_PROVIDER =
            register("strata_state_provider", StrataStateProvider.CODEC);
    public static final Supplier<BlockStateProviderType<ExtendedWeightedStateProvider>> EXTENDED_WEIGHTED_STATE_PROVIDER =
            register("extended_weighted_state_provider", ExtendedWeightedStateProvider.CODEC);

    private static <P extends BlockStateProvider> Supplier<BlockStateProviderType<P>> register(String name, MapCodec<P> codec) {
        return BLOCKSTATE_PROVIDER_TYPES.register(name, () -> new BlockStateProviderType<>(codec));
    }
}
