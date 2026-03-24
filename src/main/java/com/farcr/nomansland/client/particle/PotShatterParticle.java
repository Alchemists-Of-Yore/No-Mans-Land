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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class PotShatterParticle extends TextureSheetParticle {

    PotShatterParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        setSprite(sprite);
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
        this.quadSize /= 2.0F;
        this.lifetime = (int) (4.0 / (Math.random() * 0.9 + 0.1)) + 2;
        this.hasPhysics = true;
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
            ModelResourceLocation mrl = ModelResourceLocation.standalone(option.model().withPrefix("block/"));
            BakedModel model = manager.getModel(mrl);
            TextureAtlasSprite sprite = model.getParticleIcon(ModelData.EMPTY);
            return new PotShatterParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        }
    }
}
