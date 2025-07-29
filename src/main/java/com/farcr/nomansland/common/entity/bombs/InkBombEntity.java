package com.farcr.nomansland.common.entity.bombs;

import com.farcr.nomansland.common.entity.InkCloud;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.awt.*;

public class InkBombEntity extends ThrowableBombEntity {

    private static final float VERTICAL_RESTITUTION = 0.3F;
    private static final float HORIZONTAL_RESTITUTION = 0.4F;

    public InkBombEntity(EntityType<? extends ThrowableBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public InkBombEntity(LivingEntity livingEntity, Level level) {
        super(NMLEntities.INK_BOMB.get(), livingEntity, level);
    }

    public InkBombEntity(Level level, double x, double y, double z) {
        super(NMLEntities.INK_BOMB.get(), x, y, z, level);
    }


    private void spawnParticles(ParticleOptions particle, int amount) {
        for (int i = 0; i < amount; i++) {
            double theta = random.nextFloat() * 2 * Math.PI;
            double alpha = random.nextFloat() * 2 * Math.PI;
            double cos = Math.cos(alpha);
            double xVelocity = Math.sin(theta) * cos * (random.nextFloat() * 0.3 + 0.7);
            double yVelocity = cos * Math.cos(theta) * (random.nextFloat() * 0.3 + 0.7);
            double zVelocity = Math.sin(alpha) * (random.nextFloat() * 0.3 + 0.7);
            level().addParticle(particle, getX(), getY(), getZ(), xVelocity * 0.6, yVelocity * 0.6, zVelocity * 0.6);
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 0) {
            spawnParticles(ParticleTypes.SMOKE, 320);

            for (int i = 0; i < 40; i++) {
                double theta = random.nextFloat() * 2 * Math.PI;
                double alpha = random.nextFloat() * 2 * Math.PI;
                double cos = Math.cos(alpha);
                double xVelocity = Math.sin(theta) * cos * (random.nextFloat() * 0.3 + 0.7);
                double yVelocity = cos * Math.cos(theta) * (random.nextFloat() * 0.3 + 0.7);
                double zVelocity = Math.sin(alpha) * (random.nextFloat() * 0.3 + 0.7);
                level().addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 1), false, getX(), getY(), getZ(), xVelocity * 0.1, yVelocity * 0.1, zVelocity * 0.1);
            }
        } else if (b == 1) {
            spawnParticles(ParticleTypes.SMOKE, 400);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    protected void explode() {
        Level level = level();

        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4, (1 + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F);
        level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3.5F)).forEach(livingEntity -> {
            livingEntity.hurt(Explosion.getDefaultDamageSource(level, this), 4);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            livingEntity.removeEffect(MobEffects.INVISIBILITY);
            livingEntity.removeEffect(MobEffects.NIGHT_VISION);
        });
        InkCloud lingeringCloud = new InkCloud(level(), getX(), getY() + 1, getZ());
        Entity owner = getOwner();
        if (owner instanceof LivingEntity livingentity) {
            lingeringCloud.setOwner(livingentity);
        }

        lingeringCloud.setRadius(3);
        lingeringCloud.setWaitTime(1);
        level().addFreshEntity(lingeringCloud);
        level.broadcastEntityEvent(this, (byte) (isInWater() ? 1 : 0));
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide()) {
            explode();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 0.1) {
            setDeltaMovement(Vec3.ZERO);
            setOnGround(true);
            return;
        }

        Direction direction = result.getDirection();
        switch (direction.getAxis()) {
            case X -> setDeltaMovement(
                    -motion.x() * HORIZONTAL_RESTITUTION,
                    motion.y(),
                    motion.z()
            );
            case Y ->
                    setDeltaMovement(motion.x() * VERTICAL_RESTITUTION, -motion.y() * VERTICAL_RESTITUTION, motion.z() * VERTICAL_RESTITUTION);
            case Z -> setDeltaMovement(
                    motion.x(),
                    motion.y(),
                    -motion.z() * HORIZONTAL_RESTITUTION
            );
        }
        if (!shouldFuse()) {
            startFuse(30);
        }
    }

    @Override
    protected ParticleOptions getParticle() {
            return ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, Color.BLACK.getRGB());
    }

    @Override
    public void startFuse(int maxFuse) {
        super.startFuse(maxFuse);
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
