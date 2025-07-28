package com.farcr.nomansland.common.entity.bombs;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.entity.InkCloud;
import com.farcr.nomansland.common.entity.LingeringCloud;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.mojang.blaze3d.shaders.Effect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.awt.*;
import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.block.WallTorchBlock.FACING;

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

        level.explode(this, getX(), getY(0.0625), getZ(), 1, Level.ExplosionInteraction.NONE);
        level.getNearbyEntities(LivingEntity.class, TargetingConditions.forNonCombat(), null, getBoundingBox().inflate(4)).forEach(livingEntity -> {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200));
            livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            livingEntity.removeEffect(MobEffects.INVISIBILITY);
            livingEntity.removeEffect(MobEffects.NIGHT_VISION);
        });
        InkCloud lingeringCloud = new InkCloud(level(), getX(), getY(), getZ());
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
