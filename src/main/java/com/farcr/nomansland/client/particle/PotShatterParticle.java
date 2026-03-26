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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class PotShatterParticle extends TextureSheetParticle {

    private final float uo;
    private final float vo;
    private boolean persistent;
    private final int persistTicks;
    private final double targetX;
    private final double targetY;
    private final double targetZ;
    private final float baseQuadSize;
    private final BlockPos originBlockPos;

    private static final int LERP_TICKS = 60;

    private double settledX, settledY, settledZ;
    private boolean settled = false;

    PotShatterParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite, int persistTicks, List<List<Double>> shapeBoxes, int blockX, int blockY, int blockZ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        setSprite(sprite);
        this.gravity = 1.0F;
        this.rCol = 0.6F;
        this.gCol = 0.6F;
        this.bCol = 0.6F;
        this.quadSize /= 2.0F;
        this.baseQuadSize = this.quadSize;
        this.persistTicks = persistTicks;
        this.lifetime = persistTicks > 0 ? persistTicks + 200 : (int) (4.0 / (Math.random() * 0.9 + 0.1)) + 2;
        this.hasPhysics = true;
        this.friction = 0.98F;
        this.uo = random.nextFloat() * 3.0F;
        this.vo = random.nextFloat() * 3.0F;
        this.persistent = persistTicks > 0;
        this.originBlockPos = new BlockPos(blockX, blockY, blockZ);
        if (this.persistent && !shapeBoxes.isEmpty()) {
            double[] target = sampleShapeVolume(shapeBoxes, random);
            targetX = blockX + target[0];
            targetY = blockY + target[1];
            targetZ = blockZ + target[2];
        } else {
            targetX = x;
            targetY = y;
            targetZ = z;
        }
    }

    private static double[] sampleShapeVolume(List<List<Double>> boxes, RandomSource random) {
        double totalVolume = 0;
        for (List<Double> box : boxes) {
            double dx = box.get(3) - box.get(0);
            double dy = box.get(4) - box.get(1);
            double dz = box.get(5) - box.get(2);
            totalVolume += dx * dy * dz;
        }
        double pick = random.nextDouble() * totalVolume;
        for (List<Double> box : boxes) {
            double minX = box.get(0), minY = box.get(1), minZ = box.get(2);
            double maxX = box.get(3), maxY = box.get(4), maxZ = box.get(5);
            double vol = (maxX - minX) * (maxY - minY) * (maxZ - minZ);
            pick -= vol;
            if (pick <= 0) {
                return new double[]{
                        minX + random.nextDouble() * (maxX - minX),
                        minY + random.nextDouble() * (maxY - minY),
                        minZ + random.nextDouble() * (maxZ - minZ)
                };
            }
        }
        List<Double> last = boxes.getLast();
        return new double[]{
                last.get(0) + random.nextDouble() * (last.get(3) - last.get(0)),
                last.get(1) + random.nextDouble() * (last.get(4) - last.get(1)),
                last.get(2) + random.nextDouble() * (last.get(5) - last.get(2))
        };
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
    public void move(double x, double y, double z) {
        if (persistent) {
            double d1 = y;
            if (this.hasPhysics && (x != 0 || y != 0 || z != 0)) {
                Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(x, y, z), this.getBoundingBox(), this.level, List.of());
                x = vec3.x;
                y = vec3.y;
                z = vec3.z;
            }
            if (x != 0 || y != 0 || z != 0) {
                this.setBoundingBox(this.getBoundingBox().move(x, y, z));
                this.setLocationFromBoundingbox();
            }
            this.onGround = d1 != y && d1 < 0;
        } else {
            super.move(x, y, z);
        }
    }

    @Override
    public void tick() {
        if (!persistent) {
            super.tick();
            if (this.persistTicks > 0 && this.onGround && random.nextFloat() < 0.001F) {
                this.yd = 0.12 + random.nextDouble() * 0.08;
                this.xd += (random.nextDouble() - 0.5) * 0.03;
                this.zd += (random.nextDouble() - 0.5) * 0.03;
            }
            return;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        if (!level.getBlockState(originBlockPos).isAir()) {
            if (settled) {
                remove();
                return;
            }
            this.lifetime = this.age + (int) (4.0 / (Math.random() * 0.9 + 0.1)) + 2;
            this.persistent = false;
            return;
        }

        int settleTicks = persistTicks - LERP_TICKS;
        if (age < settleTicks) {
            this.yd -= 0.04 * this.gravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            if (this.onGround) {
                this.xd *= 0.7;
                this.zd *= 0.7;
            }

            if (this.onGround && random.nextFloat() < 0.001F) {
                this.yd = 0.12 + random.nextDouble() * 0.08;
                this.xd += (random.nextDouble() - 0.5) * 0.03;
                this.zd += (random.nextDouble() - 0.5) * 0.03;
            }
        } else {
            if (!settled) {
                settled = true;
                settledX = this.x;
                settledY = this.y;
                settledZ = this.z;
                this.xd = 0;
                this.yd = 0;
                this.zd = 0;
            }

            int tickIntoLerp = age - settleTicks;
            float progress = Mth.clamp((float) tickIntoLerp / LERP_TICKS, 0, 1);

            float eased = progress * progress * (3 - 2 * progress);

            double newX = Mth.lerp(eased, settledX, targetX);
            double newY = Mth.lerp(eased, settledY, targetY);
            double newZ = Mth.lerp(eased, settledZ, targetZ);
            this.setPos(newX, newY, newZ);
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
            return new PotShatterParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite, option.persistTicks(), option.shapeBoxes(), option.blockX(), option.blockY(), option.blockZ());
        }
    }
}
