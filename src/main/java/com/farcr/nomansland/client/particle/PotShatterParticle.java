package com.farcr.nomansland.client.particle;

import com.farcr.nomansland.common.block.pots.PotShatterParticleOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class PotShatterParticle extends TextureSheetParticle {

    private final float uo;
    private final float vo;
    private final boolean persistent;

    PotShatterParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite, int persistTicks) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        setSprite(sprite);
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
        this.quadSize /= 2.0F;
        this.lifetime = persistTicks > 0 ? persistTicks : (int) (4.0 / (Math.random() * 0.9 + 0.1)) + 2;
        this.hasPhysics = true;
        this.friction = 0.98F;
        this.uo = random.nextFloat() * 3.0F;
        this.vo = random.nextFloat() * 3.0F;
        this.persistent = persistTicks > 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (persistent && onGround && random.nextFloat() < 0.001F) {
            this.yd = 0.12 + random.nextDouble() * 0.08;
            this.xd += (random.nextDouble() - 0.5) * 0.03;
            this.zd += (random.nextDouble() - 0.5) * 0.03;
        }
    }

    @Override
    protected float getU0() {
        return sprite.getU((uo + 1.0F) / 4.0F);
    }

    @Override
    protected float getU1() {
        return sprite.getU(uo / 4.0F);
    }

    @Override
    protected float getV0() {
        return sprite.getV(vo / 4.0F);
    }

    @Override
    protected float getV1() {
        return sprite.getV((vo + 1.0F) / 4.0F);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.TERRAIN_SHEET;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<PotShatterParticleOption> {
        @Override
        public Particle createParticle(PotShatterParticleOption option, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ModelManager manager = Minecraft.getInstance().getModelManager();
            ResourceLocation loc = option.model();
            ResourceLocation resolved = loc.getPath().startsWith("entity/") ? loc : loc.withPrefix("block/");
            ModelResourceLocation mrl = ModelResourceLocation.standalone(resolved);
            BakedModel model = manager.getModel(mrl);
            TextureAtlasSprite sprite = model.getParticleIcon(ModelData.EMPTY);
            return new PotShatterParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite, option.persistTicks());
        }
    }
}
