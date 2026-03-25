package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record PotShatterParticleOption(ResourceLocation model, int persistTicks) implements ParticleOptions {

    public PotShatterParticleOption(ResourceLocation model) {
        this(model, 0);
    }

    public static MapCodec<PotShatterParticleOption> codec(ParticleType<PotShatterParticleOption> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(PotShatterParticleOption::model),
                com.mojang.serialization.Codec.INT.optionalFieldOf("persist_ticks", 0).forGetter(PotShatterParticleOption::persistTicks)
        ).apply(instance, PotShatterParticleOption::new));
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, PotShatterParticleOption> streamCodec(ParticleType<PotShatterParticleOption> type) {
        return StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, PotShatterParticleOption::model,
                ByteBufCodecs.VAR_INT, PotShatterParticleOption::persistTicks,
                PotShatterParticleOption::new
        );
    }

    @Override
    public ParticleType<?> getType() {
        return NMLParticleTypes.POT_SHATTER.get();
    }
}
