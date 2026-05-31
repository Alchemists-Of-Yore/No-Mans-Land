package com.farcr.nomansland.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.RisingParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;

public class MoonlightCandleFlameParticle extends RisingParticle {
    public MoonlightCandleFlameParticle(ClientLevel level, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, SpriteSet spriteSet) {
        super(level, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.setSprite(spriteSet.get(this.random.nextInt(4), 4));
    }

    @Override
    public void move(double pX, double pY, double pZ) {
        this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
        this.setLocationFromBoundingbox();
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        float scaleFac = ((float) this.age + pScaleFactor) / (float) this.lifetime;
        return this.quadSize * (1.0F - scaleFac * scaleFac * 0.5F);
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return LightTexture.pack(15, level.getBrightness(LightLayer.SKY, BlockPos.containing(x, y, z)));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }
}
