package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

public class FluidFallingParticle extends TextureSheetParticle {
    private final ParticleOptions flatParticle;
    public FluidFallingParticle(ClientLevel level, double x, double y, double z, SpriteSet spriteSet, ParticleOptions flatParticle) {
        super(level, x, y, z);
        this.setSprite(spriteSet.get(this.random.nextInt(4), 4));
        this.gravity = 0.01F;
        this.lifetime = (int) (16.0 / (Math.random() * 0.8 + 0.2));
        this.flatParticle = flatParticle;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.preMoveUpdate();
        if (!this.removed) {
            this.yd -= this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.postMoveUpdate();
            if (!this.removed) {
                this.xd *= 0.9800000190734863;
                this.yd *= 0.9800000190734863;
                this.zd *= 0.9800000190734863;
            }
        }
    }

    protected void preMoveUpdate() {
        if (this.lifetime-- <= 0) {
            this.remove();
        }

    }

    protected void postMoveUpdate() {
        if (this.onGround) {
            float offset = random.nextInt(-10, 10) * 0.01F;
            if (flatParticle != null) this.level.addParticle(flatParticle, this.x + (offset * Math.random()), this.y, this.z + (offset * Math.random()), 0.0, 0.0, 0.0);
            float volume = Mth.randomBetween(this.random, 0.3F, 1.0F);
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.BEEHIVE_DRIP, SoundSource.BLOCKS, volume, 1.0F, false);
            this.remove();
        }

    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}
