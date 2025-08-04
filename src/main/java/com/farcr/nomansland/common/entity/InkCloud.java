package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class InkCloud extends LingeringCloud {

    public InkCloud(EntityType<? extends LingeringCloud> entityType, Level level) {
        super(entityType, level);
    }

    public InkCloud(Level level, double x, double y, double z) {
        super(NMLEntities.INK_CLOUD.get(), level);
        setPos(x, y, z);
        setNoGravity(false);
        noPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();

        Level level = level();

        if (level.getBlockState(blockPosition().above((int) (getBbHeight() * 0.3))).isAir()) {
            Vec3 motion = getDeltaMovement();
            double dy = Mth.clamp(motion.y - 0.02, -0.04, 0);
            setDeltaMovement(motion.x, dy, motion.z);
            move(MoverType.SELF, getDeltaMovement());
        }

        if (level.isClientSide) {
            boolean isWaiting = isWaiting();
            float radius = getRadius();
            double centerY = getY() + getBbHeight() / 2.0;

            if (isWaiting && random.nextBoolean()) return;

            ParticleOptions particle = getParticle(level);
            int count = isWaiting ? 2 : Mth.ceil(Math.PI * radius * radius);
            float spread = isWaiting ? 0.2F : radius;

            for (int i = 0; i < count; i++) {
                double theta = random.nextDouble() * Mth.TWO_PI;
                double phi = Math.acos(2 * random.nextDouble() - 1);
                double r = spread * Math.cbrt(random.nextDouble());

                double sinPhi = Math.sin(phi);
                double x = getX() + r * sinPhi * Math.cos(theta);
                double y = centerY + r * Math.cos(phi);
                double z = getZ() + r * sinPhi * Math.sin(theta);

                if (isWaiting) {
                    level.addAlwaysVisibleParticle(particle, x, y, z, 0, 0, 0);
                } else {
                    level.addAlwaysVisibleParticle(particle, x, y, z, (0.5 - random.nextDouble()) * 0.15, 0.01, (0.5 - random.nextDouble()) * 0.15);
                }
            }
        }
        if (tickCount % 10 == 0) {
            double centerY = getY() + getBbHeight() / 2.0;
            Vec3 center = new Vec3(getX(), centerY, getZ());
            float radius = getRadius();

            AABB area = new AABB(
                    getX() - radius, centerY - radius, getZ() - radius,
                    getX() + radius, centerY + radius, getZ() + radius
            );

            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity entity : entities) {
                if (entity.getEyePosition().distanceToSqr(center) < Mth.square(radius) && entity.isAffectedByPotions()) {
                    boolean immune = false;
                    for (ItemStack stack : entity.getArmorSlots()) {
                        if (stack.is(NMLTags.INK_IMMUNE)) {
                            immune = true;
                            break;
                        }
                    }

                    if (!immune) {
                        entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30));
                        entity.removeEffect(MobEffects.NIGHT_VISION);
                    }

                    entity.removeEffect(MobEffects.INVISIBILITY);
                }
            }
        }
    }

    @Override
    public ParticleOptions getParticle(LevelAccessor levelAccessor) {
        return switch (random.nextInt(4)) {
            case 0 -> new DustParticleOptions(Vec3.fromRGB24(0x131110).toVector3f(), 3);
            case 1 -> new DustParticleOptions(Vec3.fromRGB24(0x111819).toVector3f(), 3);
            case 2 -> new DustParticleOptions(Vec3.fromRGB24(0x0b0a09).toVector3f(), 3);
            default -> new DustParticleOptions(Vec3.fromRGB24(0x1a1c1b).toVector3f(), 3);
        };
    }
}
