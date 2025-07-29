package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class InkCloud extends LingeringCloud {

    public InkCloud(EntityType<? extends LingeringCloud> entityType, Level level) {
        super(entityType, level);
    }

    public InkCloud(Level level, double x, double y, double z) {
        super(NMLEntities.INK_CLOUD.get(), level);
        setPos(x, y, z);
        setParticle(new DustParticleOptions(Vec3.fromRGB24(0x0b0a09).toVector3f(), 2));
    }

    @Override
    public void tick() {
        super.tick();

        Level level = level();

        if (level.isClientSide) {
            boolean isWaiting = isWaiting();
            float radius = getRadius();
            if (isWaiting && random.nextBoolean()) return;

            ParticleOptions particle = switch (random.nextInt(3)) {
                case 0 -> new DustParticleOptions(Vec3.fromRGB24(0x131110).toVector3f(), 2);
                case 1 -> new DustParticleOptions(Vec3.fromRGB24(0x111819).toVector3f(), 2);
                default -> new DustParticleOptions(Vec3.fromRGB24(0x1a1c1b).toVector3f(), 2);
            };
            int count = isWaiting ? 2 : Mth.ceil(Math.PI * radius * radius)*2;
            float spread = isWaiting ? 0.2F : radius;

            for (int i = 0; i < count; i++) {
                double theta = random.nextDouble() * Mth.TWO_PI;
                double phi = Math.acos(2 * random.nextDouble() - 1);
                double r = spread * Math.cbrt(random.nextDouble());

                double sinPhi = Math.sin(phi);
                double x = getX() + r * sinPhi * Math.cos(theta);
                double y = getY() + r * Math.cos(phi);
                double z = getZ() + r * sinPhi * Math.sin(theta);

                if (isWaiting) {
                    level.addAlwaysVisibleParticle(particle, x, y, z, 0, 0, 0);
                } else {
                    level.addAlwaysVisibleParticle(particle, x, y, z, (0.5 - random.nextDouble()) * 0.15, 0.01, (0.5 - random.nextDouble()) * 0.15);
                }
            }
        }

        if (tickCount % 10 == 0) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, getBoundingBox());
            for (LivingEntity entity : entities) {
                if (entity.getEyePosition().distanceToSqr(position()) < Mth.square(getRadius()) && entity.isAffectedByPotions()) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30));
                    entity.removeEffect(MobEffects.INVISIBILITY);
                    entity.removeEffect(MobEffects.NIGHT_VISION);

                }
            }
        }
    }
}
