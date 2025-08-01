package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class TranslucentDustParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public TranslucentDustParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TranslucentDustParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        this.xd *= 0.10000000149011612;
        this.yd *= 0.10000000149011612;
        this.zd *= 0.10000000149011612;
        this.quadSize = this.quadSize * 0.75F;
        int i = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.lifetime = Math.max(i, 1);
        this.setSpriteFromAge(sprites);
        float f = this.random.nextFloat() * 0.4F + 0.6F;
        this.rCol = this.randomizeColor(options.getRed(), f);
        this.gCol = this.randomizeColor(options.getGreen(), f);
        this.bCol = this.randomizeColor(options.getGreen(), f);
        this.alpha = options.getAlpha();
    }

    protected float randomizeColor(float coordMultiplier, float multiplier) {
        return (this.random.nextFloat() * 0.2F + 0.8F) * coordMultiplier * multiplier;
    }

    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public float getQuadSize(float scaleFactor) {
        return this.quadSize * Mth.clamp(((float)this.age + scaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<TranslucentDustParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        public Particle createParticle(TranslucentDustParticleOptions type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new TranslucentDustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type, this.sprites);
        }
    }
}
