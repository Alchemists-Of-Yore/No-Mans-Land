package com.farcr.nomansland.common.entity.bombs;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.pots.PotShatterParticleOption;
import com.farcr.nomansland.common.entity.PacifiedCloud;
import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class LivingUrn extends ThrowableBombEntity {

    private static final float VERTICAL_RESTITUTION = 0.3F;
    private static final float HORIZONTAL_RESTITUTION = 0.4F;

    private int bounceCooldown = -1;
    private float shakeTimer = 0;

    public LivingUrn(EntityType<? extends ThrowableBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LivingUrn(LivingEntity livingEntity, Level level) {
        super(NMLEntities.LIVING_URN.get(), livingEntity, level);
    }

    public LivingUrn(Level level, double x, double y, double z) {
        super(NMLEntities.LIVING_URN.get(), x, y, z, level);
    }

    private static final ResourceLocation URN_MODEL = NoMansLand.location("entity/living_urn");

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

    private void spawnShatterParticles(int count, double spread) {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new PotShatterParticleOption(URN_MODEL),
                    getX(), getY() + getBbHeight() * 0.5, getZ(),
                    count, spread, spread, spread, 0.15);
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
                level().addParticle(ParticleTypes.FLAME, false, getX(), getY(), getZ(), xVelocity * 0.1, yVelocity * 0.1, zVelocity * 0.1);
            }
        } else if (b == 1) {
            spawnParticles(ParticleTypes.SMOKE, 400);
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    protected void explode() {
        PacifiedCloud pacifiedCloud = new PacifiedCloud(level(), getX(), getY() - 1, getZ());
        Entity owner = getOwner();
        if (owner instanceof LivingEntity livingentity) {
            pacifiedCloud.setOwner(livingentity);
        }

        pacifiedCloud.setRadius(2.5F);
        pacifiedCloud.setWaitTime(0);
        pacifiedCloud.setDuration(120);
        pacifiedCloud.setRadiusPerTick((float) -1/100);
        pacifiedCloud.setPotionContents(new PotionContents(Optional.empty(), Optional.of(1), List.of(new MobEffectInstance(NMLEffects.PACIFIED, 1200, 0, false, false))));
        level().addFreshEntity(pacifiedCloud);
        level().playSound(null, blockPosition(), NMLSounds.LIVING_URN_SHATTERS.get(), SoundSource.PLAYERS, 1, 0.75F);
        spawnShatterParticles(40, 0.25);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    NMLParticleTypes.LIVING_URN_SHARD_FACE.get(),
                    getX(), getY() + getBbHeight() * 0.5, getZ(),
                    1, 0.0, 0.0, 0.0, 0.0);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 0.1) {
            setDeltaMovement(Vec3.ZERO);
            setOnGround(true);
            if (motion.x != 0 && motion.z != 0) {
                bounceCooldown = 60;
                shakeTimer = 1;
            }
            return;
        }

        spawnShatterParticles(4, 0.1);

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
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof Mob mob && result.getEntity() instanceof Enemy && !(result.getEntity() instanceof NeutralMob)) {
            mob.addEffect(new MobEffectInstance(NMLEffects.PACIFIED, 1200, 0, false, false));
            explode();
        } else {
            spawnShatterParticles(4, 0.1);
            Vec3 motion = getDeltaMovement();

            Vec3 normal = position().subtract(result.getEntity().position()).normalize(); // Collision normal
            double dot = motion.dot(normal);

            Vec3 reflected = motion.subtract(normal.scale(2 * dot));
            Vec3 bounced = reflected.scale(HORIZONTAL_RESTITUTION);

            setDeltaMovement(bounced);
        }
    }

    @Override
    protected void updateRotation() {
        if (bounceCooldown > 0) return;

        super.updateRotation();
    }

    @Override
    public void tick() {
        super.tick();

        Level level = level();

        Mob mob = level.getNearestEntity(
                Mob.class,
                TargetingConditions.DEFAULT.range(8),
                null, getX(), getY(), getZ(),
                new AABB(blockPosition()).inflate(8)
        );

        if (level.isClientSide()) {
            if (!onGround()) {
                level.addParticle(getParticle(level), getX(), getY() + getBbHeight(), getZ(), 0, 0, 0);
            }

            if (shakeTimer > 0) {
                if (mob instanceof Enemy && !(mob instanceof NeutralMob) && !mob.hasEffect(NMLEffects.PACIFIED)) {
                    if (bounceCooldown > 0) {
                        if (bounceCooldown < 45) {
                            Vec3 toTarget = mob.position().subtract(position());
                            if (toTarget.lengthSqr() > 0.001) {
                                double dx = toTarget.x;
                                double dz = toTarget.z;

                                float targetYaw = (float) (Mth.atan2(-dx, -dz) * (180F / Math.PI)) + 90;
                                float currentYaw = getYRot();
                                float newYaw = Mth.approachDegrees(currentYaw, targetYaw, 5);

                                if (Math.abs(Mth.degreesDifference(currentYaw, targetYaw)) > 1f)
                                    setYRot(newYaw);
                            }
                        }

                        if (bounceCooldown < 15) {
                            shakeTimer++;

                            float amplitude = Math.min(12, shakeTimer * 0.8F);
                            roll += (float) (Math.sin(shakeTimer) * amplitude);
                        }
                    }
                }

                float delta = -normalizeAngle(roll);
                roll += delta * 0.075f;
            } else {
                double motionLen = getDeltaMovement().lengthSqr();
                if (motionLen > 0.01) {
                    roll += Math.sqrt(motionLen) * 45;
                }
            }
        }

        if (bounceCooldown > 0) {
            bounceCooldown--;

            if (bounceCooldown == 0) {
                if (mob instanceof Enemy && !(mob instanceof NeutralMob) && !mob.hasEffect(NMLEffects.PACIFIED)) {
                    Vec3 toTarget = mob.position().subtract(position());
                    double distance = toTarget.length();

                    if (distance > 0.01) {
                        Vec3 direction = toTarget.normalize();

                        double minSpeed = 0.1;
                        double maxSpeed = 0.4;
                        double minJump = 0.05;
                        double maxJump = 0.5;

                        double clampedDistance = Math.min(8.0, distance);
                        double speed = minSpeed + (maxSpeed - minSpeed) * clampedDistance / 8.0;
                        double jumpStrength = minJump + (maxJump - minJump) * clampedDistance / 8.0;

                        Vec3 jumpImpulse = new Vec3(
                                direction.x * speed,
                                jumpStrength,
                                direction.z * speed
                        );

                        float targetYaw = (float) (Mth.atan2(-toTarget.x, -toTarget.z) * (180F / Math.PI)) + 90;
                        setYRot(targetYaw);

                        setDeltaMovement(jumpImpulse);
                        setOnGround(false);
                        bounceCooldown = -1;
                    }
                } else {
                    level.playSound(null, blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.75F);
                    if (!level.isClientSide) ((ServerLevel) level).sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 10, 0, 0, 0, 0.02);
                    level.addFreshEntity(new ItemEntity(level, getX(), getY(), getZ(), NMLItems.LIVING_URN.stack()));
                    discard();
                }
            }
        }
    }

    @Override
    protected ParticleOptions getParticle(LevelAccessor levelAccessor) {
        return switch (levelAccessor.getRandom().nextInt(6)) {
            case 0 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xc6ff82));
            case 1 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xffdd82));
            case 2 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xfeb3bc));
            case 3 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xfe82ff));
            case 4 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xd682ff));
            default -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0x82ffc0));
        };
    }

    private float normalizeAngle(float angle) {
        angle %= 360;
        if (angle > 180) angle -= 360;
        if (angle < -180) angle += 360;
        return angle;
    }
}
