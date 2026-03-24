package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record PotShatterParticleOption(ResourceLocation model) implements ParticleOptions {

    public static MapCodec<PotShatterParticleOption> codec(ParticleType<PotShatterParticleOption> type) {
        return ResourceLocation.CODEC.xmap(
                PotShatterParticleOption::new,
                PotShatterParticleOption::model
        ).fieldOf("model");
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, PotShatterParticleOption> streamCodec(ParticleType<PotShatterParticleOption> type) {
        return ResourceLocation.STREAM_CODEC.map(PotShatterParticleOption::new, PotShatterParticleOption::model);
    }

    @Override
    public ParticleType<?> getType() {
        return NMLParticleTypes.POT_SHATTER.get();
    }
}
