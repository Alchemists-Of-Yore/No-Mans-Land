package com.farcr.nomansland.client.particle;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.FastColor;

public class TranslucentDustParticleOptions implements ParticleOptions {
    public final int color;

    public static MapCodec<TranslucentDustParticleOptions> codec(ParticleType<TranslucentDustParticleOptions> particleType) {
        return ExtraCodecs.ARGB_COLOR_CODEC.xmap(
                TranslucentDustParticleOptions::new,
                (o) -> o.color).fieldOf("color");
    }

    public static StreamCodec<? super ByteBuf, TranslucentDustParticleOptions> streamCodec(ParticleType<TranslucentDustParticleOptions> type) {
        return ByteBufCodecs.INT.map(TranslucentDustParticleOptions::new, (o) -> o.color);
    }

    private TranslucentDustParticleOptions(int color) {
        this.color = color;
    }

    public ParticleType<TranslucentDustParticleOptions> getType() {
        return NMLParticleTypes.TRANSLUCENT_DUST.get();
    }

    public float getRed() {
        return (float) FastColor.ARGB32.red(this.color) / 255.0F;
    }

    public float getGreen() {
        return (float) FastColor.ARGB32.green(this.color) / 255.0F;
    }

    public float getBlue() {
        return (float) FastColor.ARGB32.blue(this.color) / 255.0F;
    }

    public float getAlpha() {
        return (float) FastColor.ARGB32.alpha(this.color) / 255.0F;
    }

    public static TranslucentDustParticleOptions create(int color) {
        return new TranslucentDustParticleOptions(color);
    }

    public static TranslucentDustParticleOptions create(float red, float green, float blue) {
        return create(FastColor.ARGB32.colorFromFloat(1, red, green, blue));
    }
}
