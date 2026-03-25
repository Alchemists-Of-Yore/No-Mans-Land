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
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class PotShatterParticle extends TextureSheetParticle {

    private final float uo;
    private final float vo;
    private final double originX;
    private final double originY;
    private final double originZ;
    private final boolean persistent;
    private static final int GROUP_UP_TICKS = 20;

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
        this.uo = random.nextFloat() * 3.0F;
        this.vo = random.nextFloat() * 3.0F;
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.persistent = persistTicks > 0;
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
    public void tick() {
        super.tick();

        if (persistent) {
            BlockPos originPos = BlockPos.containing(originX, originY, originZ);
            if (!level.getBlockState(originPos).isAir()) {
                remove();
                return;
            }

            int ticksLeft = lifetime - age;

            if (random.nextFloat() < 0.03F) {
                this.yd += 0.04 + random.nextDouble() * 0.06;
                this.xd += (random.nextDouble() - 0.5) * 0.02;
                this.zd += (random.nextDouble() - 0.5) * 0.02;
            }

            if (ticksLeft <= GROUP_UP_TICKS) {
                double progress = 1.0 - (double) ticksLeft / GROUP_UP_TICKS;
                double pullStrength = 0.05 * progress;
                this.xd += (originX - this.x) * pullStrength;
                this.yd += (originY - this.y) * pullStrength;
                this.zd += (originZ - this.z) * pullStrength;
                this.gravity = 0;
            }
        } else {
            if (this.onGround) {
                this.xd = 0;
                this.zd = 0;
                this.yd = 0;
            }
        }
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
            return new PotShatterParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite, option.persistTicks());
        }
    }
}
