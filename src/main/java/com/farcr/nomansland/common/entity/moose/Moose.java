package com.farcr.nomansland.common.entity.moose;

import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Moose extends PathfinderMob implements PlayerRideable, Saddleable {

    private static final EntityDataAccessor<Boolean> DATA_HAS_ANTLERS = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_SADDLED = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);

    private int pacificationStage;

    public Moose(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_HAS_ANTLERS, true);
        builder.define(DATA_IS_SADDLED, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putInt("PacificationStage", getPacificationStage());
        compound.putBoolean("HasAntlers", hasAntlers());
        compound.putBoolean("IsSaddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        setPacificationStage(compound.getInt("PacificationStage"));
        setHasAntlers(compound.getBoolean("HasAntlers"));
        setIsSaddled(compound.getBoolean("IsSaddled"));
    }

    @Override
    protected Brain.Provider<Moose> brainProvider() {
        return MooseAI.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return MooseAI.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<Moose> getBrain() {
        return (Brain<Moose>) super.getBrain();
    }

    @Override
    protected void customServerAiStep() {
        level().getProfiler().push("mooseBrain");
        getBrain().tick((ServerLevel) level(), this);
        level().getProfiler().pop();
        level().getProfiler().push("mooseActivityUpdate");
        MooseAI.updateActivity(this);
        level().getProfiler().pop();

        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.is(Items.SADDLE) && !isBaby() && !isSaddled()) {
            if (!level().isClientSide) {
                equipSaddle(stack, SoundSource.NEUTRAL);
                stack.shrink(1);
            }

            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        if (isPacified()) {
            if (isSaddled() && !isVehicle()) {
                doPlayerRide(player);

                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        } else {
            if (getTarget() == null && stack.is(Items.GOLDEN_CARROT)) {
                if (!level().isClientSide) {
                    int stage = getPacificationStage();
                    stack.shrink(1);
                    if (stage < 4) {
                        setPacificationStage(stage + 1);
                        level().broadcastEntityEvent(this, (byte) 6);
                    } else {
                        if (random.nextInt(3) == 0) {
                            setPacificationStage(5);
                            navigation.stop();
                            setTarget(null);
                            brain.getMemory(MemoryModuleType.ANGRY_AT).ifPresent(target -> {
                                if (player.getUUID().equals(target))
                                    brain.eraseMemory(MemoryModuleType.ANGRY_AT);
                            });
                            level().broadcastEntityEvent(this, (byte) 7);
                        } else {
                            level().broadcastEntityEvent(this, (byte) 6);
                        }
                    }
                }

                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    protected void doPlayerRide(Player player) {
        if (!level().isClientSide) {
            player.setYRot(getYRot());
            player.setXRot(getXRot());
            player.startRiding(this);
        }
    }

    @Override
    protected void tickRidden(Player player, Vec3 travelVector) {
        super.tickRidden(player, travelVector);

        Vec2 vec2 = getRiddenRotation(player);
        setRot(vec2.y, vec2.x);
        yRotO = yBodyRot = yHeadRot = getYRot();
    }

    protected Vec2 getRiddenRotation(LivingEntity entity) {
        return new Vec2(entity.getXRot() * 0.5F, entity.getYRot());
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 travelVector) {
        float x = player.xxa * 0.5F;
        float z = player.zza;
        if (z <= 0.0F) {
            z *= 0.25F;
        }

        return new Vec3(x, 0, z);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }


    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 direction, LivingEntity passenger) {
        double x = getX() + direction.x;
        double y = getBoundingBox().minY;
        double z = getZ() + direction.z;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (Pose pose : passenger.getDismountPoses()) {
            mutablePos.set(x, y, z);
            double d3 = getBoundingBox().maxY + (double) 0.75F;

            while (true) {
                double d4 = level().getBlockFloorHeight(mutablePos);
                if ((double) mutablePos.getY() + d4 > d3) {
                    break;
                }

                if (DismountHelper.isBlockFloorValid(d4)) {
                    AABB aabb = passenger.getLocalBoundsForPose(pose);
                    Vec3 vec3 = new Vec3(x, (double) mutablePos.getY() + d4, z);
                    if (DismountHelper.canDismountTo(level(), passenger, aabb.move(vec3))) {
                        passenger.setPose(pose);
                        return vec3;
                    }
                }

                mutablePos.move(Direction.UP);
                if ((double) mutablePos.getY() < d3) {
                    break;
                }
            }
        }

        return null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3 escapeVector = getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), getYRot() + (livingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F));
        Vec3 dismountLocation = getDismountLocationInDirection(escapeVector, livingEntity);
        if (dismountLocation != null) {
            return dismountLocation;
        } else {
            escapeVector = getCollisionHorizontalEscapeVector(getBbWidth(), livingEntity.getBbWidth(), getYRot() + (livingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F));
            dismountLocation = getDismountLocationInDirection(escapeVector, livingEntity);
            return dismountLocation != null ? dismountLocation : position();
        }
    }


    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            spawnTamingParticles(true);
        } else if (id == 6) {
            spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    protected void spawnTamingParticles(boolean tamed) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!tamed) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d0 = random.nextGaussian() * 0.02;
            double d1 = random.nextGaussian() * 0.02;
            double d2 = random.nextGaussian() * 0.02;
            level().addParticle(particleoptions, getRandomX((double)1.0F), getRandomY() + (double)0.5F, getRandomZ((double)1.0F), d0, d1, d2);
        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        if (isSaddled()) {
            Entity entity = getFirstPassenger();
            if (entity instanceof Player) {
                return (Player)entity;
            }
        }

        return super.getControllingPassenger();
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        double power = getAttributeValue(Attributes.ATTACK_DAMAGE);
        double damage = power > 0 ? power / 2 + random.nextInt((int) power) : power;
        DamageSource damagesource = damageSources().mobAttack(this);
        boolean hurt = entity.hurt(damagesource, (float) damage);
        if (hurt) {
            double knockbackResistance;
            if (entity instanceof LivingEntity livingentity)
                knockbackResistance = livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            else knockbackResistance = 0;

            entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.4F * Math.max(0, 1 - knockbackResistance), 0));

            if (level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damagesource);
            }
        }

        return hurt;
    }

    public int getPacificationStage() {
        return pacificationStage;
    }

    public void setPacificationStage(int stage) {
        pacificationStage = stage;
    }

    public boolean isPacified() {
        return getPacificationStage() == 5;
    }

    public boolean hasAntlers() {
        return entityData.get(DATA_HAS_ANTLERS);
    }

    public void setHasAntlers(boolean hasAntlers) {
        entityData.set(DATA_HAS_ANTLERS, hasAntlers);
    }

    @Override
    public boolean isSaddleable() {
        return isPacified();
    }

    @Override
    public void equipSaddle(ItemStack itemStack, @Nullable SoundSource soundSource) {
        setIsSaddled(true);
    }

    public void shakeOffSaddle() {
        setIsSaddled(false);
        ItemEntity itementity = spawnAtLocation(Items.SADDLE, 1);
        if (itementity != null) {
            itementity.setDeltaMovement(itementity.getDeltaMovement().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
        }
    }

    @Override
    public boolean isSaddled() {
        return entityData.get(DATA_IS_SADDLED);
    }

    public void setIsSaddled(boolean isSaddled) {
        entityData.set(DATA_IS_SADDLED, isSaddled);
    }
}