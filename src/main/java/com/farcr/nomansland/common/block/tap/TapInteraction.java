package com.farcr.nomansland.common.block.tap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;
import java.util.Optional;

public record TapInteraction(List<BlockStateProvider> sources, Block cauldron, float rate, Optional<ParticleType<?>> particleType) {
    public static final Codec<TapInteraction> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BlockStateProvider.CODEC.listOf().fieldOf("sources").forGetter(TapInteraction::sources),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("cauldron").forGetter(TapInteraction::cauldron),
            Codec.floatRange(0, 10F).fieldOf("rate").forGetter(TapInteraction::rate),
            BuiltInRegistries.PARTICLE_TYPE.byNameCodec().optionalFieldOf("particle").forGetter(TapInteraction::particleType)
    ).apply(instance, TapInteraction::new));
}
