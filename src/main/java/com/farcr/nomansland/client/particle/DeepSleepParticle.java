package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.NotNull;

public class DeepSleepParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;

    public DeepSleepParticle(
        ClientLevel level, double pX, double pY, double pZ,
        double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet spriteSet
    ) {
        super(level, pX, pY, pZ, 0f, 0f, 0f);
        this.spriteSet = spriteSet;
        this.lifetime = 45;
        this.setSprite(spriteSet.get(1, 1));
    }

    @Override
    public void tick() {}

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }
}
