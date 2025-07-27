package com.farcr.nomansland.common.entity.bombs;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

import static net.minecraft.world.level.block.WallTorchBlock.FACING;

public class LivingUrnEntity extends ThrowableBombEntity {

    private static final float VERTICAL_RESTITUTION = 0.3F;
    private static final float HORIZONTAL_RESTITUTION = 0.4F;
    private int bounceCooldown;

    public LivingUrnEntity(EntityType<? extends ThrowableBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public LivingUrnEntity(LivingEntity livingEntity, Level level) {
        super(NMLEntities.LIVING_URN.get(), livingEntity, level);
    }

    public LivingUrnEntity(Level level, double x, double y, double z) {
        super(NMLEntities.LIVING_URN.get(), x, y, z, level);
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
        level().playSound(null, blockPosition(), SoundEvents.MUD_BRICKS_BREAK, SoundSource.PLAYERS, 1, 1);
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() < 0.1) {
            if (motion.x != 0 && motion.z != 0) bounceCooldown = 30;
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
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!level().isClientSide() && result.getEntity() instanceof Monster) {
            explode();
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (bounceCooldown > 0) {
            bounceCooldown--;

            if (bounceCooldown == 0) {
                Monster monster = level().getNearestEntity(Monster.class, TargetingConditions.DEFAULT.range(8), null, blockPosition().getX(), blockPosition().getY(), blockPosition().getZ(), new AABB(blockPosition()).inflate(8));

                if (monster != null) {
                    Vec3 toTarget = monster.position().subtract(position());
                    double distance = toTarget.length();

                    if (distance > 0.01) {
                        Vec3 direction = toTarget.normalize();

                        double minSpeed = 0.1;
                        double maxSpeed = 0.4;

                        double minJump = 0.05;
                        double maxJump = 0.5;

                        double speed = minSpeed + (maxSpeed - minSpeed) * distance / 8;
                        double jumpStrength = minJump + (maxJump - minJump) * distance / 8;

                        Vec3 jumpImpulse = new Vec3(
                                direction.x * speed,
                                jumpStrength,
                                direction.z * speed
                        );

                        setDeltaMovement(jumpImpulse);
                        setOnGround(false);
                        bounceCooldown = -1;
                    }
                } else {
                    level().addFreshEntity(new ItemEntity(level(), position().x, position().y, position().z, NMLItems.LIVING_URN.stack()));
                    discard();
                }
            }
        }
    }

    @Override
    protected ParticleOptions getParticle() {
        return ParticleTypes.HEART;
    }
}
