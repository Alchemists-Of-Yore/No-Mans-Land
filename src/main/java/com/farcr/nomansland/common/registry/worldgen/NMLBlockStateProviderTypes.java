package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.blockstateproviders.StrataStateProvider;
import com.farcr.nomansland.common.world.blockstateproviders.VerticalGradientStateProvider;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLBlockStateProviderTypes {
    public static final DeferredRegister<BlockStateProviderType<?>> BLOCKSTATE_PROVIDER_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE, NoMansLand.MODID);

    public static final Supplier<BlockStateProviderType<VerticalGradientStateProvider>> VERTICAL_GRADIENT =
            register("vertical_gradient", VerticalGradientStateProvider.CODEC);
    public static final Supplier<BlockStateProviderType<StrataStateProvider>> STRATA =
            register("strata", StrataStateProvider.CODEC);

    private static <P extends BlockStateProvider> Supplier<BlockStateProviderType<P>> register(String name, MapCodec<P> codec) {
        return BLOCKSTATE_PROVIDER_TYPES.register(name, () -> new BlockStateProviderType<>(codec));
    }
}
