package com.farcr.nomansland.common.entity.beetle;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Beetle extends Animal {
    public static final int STATE_IDLE = 0;
    public static final int STATE_PUSHING = 1;
    public static final int STATE_FLYING = 2;
    public static final int STATE_FLIPPED = 3;

    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(Beetle.class, EntityDataSerializers.INT);

    private int flightTicks;
    private int flipTicks;
    private int notBotheredTicks;
    private int flightCooldown;
    private int flightGrace;
    private boolean flightLeftGround;
    private double flightStartY;
    private double flightTargetX;
    private double flightTargetY;
    private double flightTargetZ;
    @Nullable
    private UUID dungBallUUID;

    public float flipAmount;
    public float flipAmountO;
    public float wingAmount;
    public float wingAmountO;

    public Beetle(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ARMOR, 5)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    public static boolean checkBeetleSpawnRules(EntityType<? extends Animal> type, LevelReader level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).isSolidRender(level, pos.below()) && level.getBlockState(pos).isAir();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, STATE_IDLE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new BeetleIncapacitatedGoal(this));
        goalSelector.addGoal(2, new PanicGoal(this, 1.6));
        goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        goalSelector.addGoal(4, new TemptGoal(this, 1.1, Ingredient.of(NMLItems.GRUBROOT.get()), false));
        goalSelector.addGoal(5, new BeetleFlyToReachGoal(this));
        goalSelector.addGoal(6, new BeetlePushDungGoal(this, 1.0));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    public int getState() {
        return entityData.get(DATA_STATE);
    }

    public void setState(int state) {
        entityData.set(DATA_STATE, state);
    }

    public boolean isIncapacitated() {
        int state = getState();
        return state == STATE_FLYING || state == STATE_FLIPPED;
    }

    public boolean isFlying() {
        return getState() == STATE_FLYING;
    }

    public boolean isFlipped() {
        return getState() == STATE_FLIPPED;
    }

    public boolean isPushing() {
        return getState() == STATE_PUSHING;
    }

    public int getFlightCooldown() {
        return flightCooldown;
    }

    public void startFlight(double targetX, double targetY, double targetZ) {
        if (isIncapacitated() || !hasFlightHeadroom()) return;
        abandonDungBall();
        setState(STATE_FLYING);
        flightTicks = 50 + random.nextInt(30);
        flightGrace = 8;
        flightLeftGround = false;
        flightStartY = getY();
        flightTargetX = targetX;
        flightTargetY = targetY;
        flightTargetZ = targetZ;
        Vec3 direction = new Vec3(targetX - getX(), 0.0, targetZ - getZ());
        direction = direction.lengthSqr() > 1.0E-4 ? direction.normalize() : Vec3.ZERO;
        setDeltaMovement(direction.x * 0.25, 0.4, direction.z * 0.25);
        hasImpulse = true;
        getNavigation().stop();
        playSound(SoundEvents.BEE_LOOP, 0.6F, 0.9F + random.nextFloat() * 0.2F);
    }

    public void flee(double fromX, double fromZ) {
        Vec3 away = new Vec3(getX() - fromX, 0.0, getZ() - fromZ);
        if (away.lengthSqr() < 1.0E-4) {
            away = new Vec3(random.nextDouble() - 0.5, 0.0, random.nextDouble() - 0.5);
        }
        away = away.normalize();
        startFlight(getX() + away.x * 7.0, getY(), getZ() + away.z * 7.0);
    }

    private void startFlip() {
        setState(STATE_FLIPPED);
        flipTicks = 40 + random.nextInt(40);
        getNavigation().stop();
    }

    public boolean hasFlightHeadroom() {
        return hasHeadroomAt(blockPosition());
    }

    private boolean hasHeadroomAt(BlockPos standPos) {
        for (int i = 1; i <= 2; i++) {
            BlockPos above = standPos.above(i);
            if (!level().getBlockState(above).getCollisionShape(level(), above).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public BlockPos findOpenSkySpot() {
        for (int attempt = 0; attempt < 10; attempt++) {
            BlockPos around = blockPosition().offset(random.nextInt(11) - 5, 0, random.nextInt(11) - 5);
            BlockPos stand = standableWithHeadroom(around);
            if (stand != null) {
                Path path = getNavigation().createPath(stand, 0);
                if (path != null && path.canReach()) {
                    return stand;
                }
            }
        }
        return null;
    }

    @Nullable
    private BlockPos standableWithHeadroom(BlockPos around) {
        for (int y = 2; y >= -3; y--) {
            BlockPos pos = around.offset(0, y, 0);
            BlockPos below = pos.below();
            if (level().getBlockState(below).isSolidRender(level(), below)
                    && level().getBlockState(pos).getCollisionShape(level(), pos).isEmpty()
                    && hasHeadroomAt(pos)) {
                return pos;
            }
        }
        return null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !level().isClientSide && isAlive() && !isIncapacitated()
                && !source.is(DamageTypes.DROWN) && !source.is(DamageTypes.IN_WALL)) {
            notBotheredTicks = 0;
            if (random.nextFloat() < 0.55F) {
                double fromX = getX();
                double fromZ = getZ();
                Vec3 sourcePos = source.getSourcePosition();
                if (sourcePos != null) {
                    fromX = sourcePos.x;
                    fromZ = sourcePos.z;
                }
                flee(fromX, fromZ);
            }
        }
        return result;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void customServerAiStep() {
        int state = getState();
        if (state == STATE_IDLE || state == STATE_PUSHING) {
            notBotheredTicks++;
            if (flightCooldown > 0) flightCooldown--;
        }

        if (state == STATE_FLYING) {
            tickFlight();
        } else if (state == STATE_FLIPPED) {
            flipTicks--;
            getNavigation().stop();
            Vec3 motion = getDeltaMovement();
            setDeltaMovement(motion.x * 0.6, motion.y, motion.z * 0.6);
            if (flipTicks <= 0) {
                setState(STATE_IDLE);
                flightCooldown = 200 + random.nextInt(200);
                notBotheredTicks = 0;
            }
        }

        super.customServerAiStep();
    }

    private void tickFlight() {
        flightTicks--;
        if (!flightLeftGround) {
            if (!onGround()) {
                flightLeftGround = true;
            } else if (--flightGrace <= 0) {
                land(false);
                return;
            }
        }

        double dx = flightTargetX - getX();
        double dz = flightTargetZ - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        boolean arrived = horizontal < 1.4 || flightTicks <= 0;

        Vec3 motion = getDeltaMovement();
        double vx = motion.x;
        double vy = motion.y;
        double vz = motion.z;

        if (!arrived) {
            double inv = horizontal > 1.0E-4 ? 1.0 / horizontal : 0.0;
            vx = vx * 0.82 + dx * inv * 0.05;
            vz = vz * 0.82 + dz * inv * 0.05;
            double climbTo = Math.max(flightStartY, flightTargetY) + 2.0;
            if (getY() < climbTo) {
                vy = Math.min(vy + 0.07, 0.32);
            } else {
                vy = vy * 0.9 - 0.015;
            }
        } else {
            vx *= 0.8;
            vz *= 0.8;
        }

        setDeltaMovement(Mth.clamp(vx, -0.4, 0.4), Mth.clamp(vy, -0.5, 0.4), Mth.clamp(vz, -0.4, 0.4));
        hasImpulse = true;

        boolean bumped = horizontalCollision;
        if (!bumped) {
            for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.1))) {
                if (living != this) {
                    living.push(this);
                    bumped = true;
                    break;
                }
            }
        }

        if (bumped && !onGround()) {
            startFlip();
        } else if (flightLeftGround && onGround()) {
            land(arrived);
        } else if (flightTicks <= -100) {
            land(false);
        }
    }

    private void land(boolean reachedTarget) {
        setState(STATE_IDLE);
        flightCooldown = reachedTarget ? 200 + random.nextInt(200) : 40 + random.nextInt(40);
        notBotheredTicks = 0;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            flipAmountO = flipAmount;
            wingAmountO = wingAmount;
            flipAmount = Mth.clamp(flipAmount + (isFlipped() ? 0.2F : -0.2F), 0.0F, 1.0F);
            wingAmount = Mth.clamp(wingAmount + (isFlying() ? 0.25F : -0.15F), 0.0F, 1.0F);
        }
    }

    public float getFlipAmount(float partialTick) {
        return Mth.lerp(partialTick, flipAmountO, flipAmount);
    }

    public float getWingAmount(float partialTick) {
        return Mth.lerp(partialTick, wingAmountO, wingAmount);
    }

    public boolean readyForDungBall() {
        return getState() == STATE_IDLE && notBotheredTicks > 600 && isAlive() && !isBaby();
    }

    public void resetBotherTimer() {
        notBotheredTicks = 0;
    }

    @Nullable
    public DungBall getDungBall() {
        if (dungBallUUID == null || !(level() instanceof ServerLevel serverLevel)) return null;
        if (serverLevel.getEntity(dungBallUUID) instanceof DungBall ball && ball.isAlive() && ball.isOwnedBy(this)) {
            return ball;
        }
        dungBallUUID = null;
        return null;
    }

    public void setDungBall(@Nullable DungBall ball) {
        dungBallUUID = ball == null ? null : ball.getUUID();
    }

    public void abandonDungBall() {
        DungBall ball = getDungBall();
        if (ball != null) ball.setOwner(null);
        dungBallUUID = null;
        if (getState() == STATE_PUSHING) setState(STATE_IDLE);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType != MobSpawnType.BREEDING && random.nextFloat() < 0.25F) {
            notBotheredTicks = 600;
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(NMLItems.GRUBROOT.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob otherParent) {
        Grub grub = NMLEntities.GRUB.get().create(serverLevel);
        if (grub != null) {
            grub.setBaby(true);
            grub.setPendingVariant(NoMansLand.location("grub/beetle"));
        }
        return grub;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SPIDER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BeetleState", getState());
        compound.putInt("FlightTicks", flightTicks);
        compound.putInt("FlipTicks", flipTicks);
        compound.putInt("FlightCooldown", flightCooldown);
        compound.putInt("NotBotheredTicks", notBotheredTicks);
        compound.putBoolean("FlightLeftGround", flightLeftGround);
        compound.putDouble("FlightStartY", flightStartY);
        compound.putDouble("FlightTargetX", flightTargetX);
        compound.putDouble("FlightTargetY", flightTargetY);
        compound.putDouble("FlightTargetZ", flightTargetZ);
        if (dungBallUUID != null) compound.putUUID("DungBall", dungBallUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        setState(compound.getInt("BeetleState"));
        flightTicks = compound.getInt("FlightTicks");
        flipTicks = compound.getInt("FlipTicks");
        flightCooldown = compound.getInt("FlightCooldown");
        notBotheredTicks = compound.getInt("NotBotheredTicks");
        flightLeftGround = compound.getBoolean("FlightLeftGround");
        flightStartY = compound.getDouble("FlightStartY");
        flightTargetX = compound.getDouble("FlightTargetX");
        flightTargetY = compound.getDouble("FlightTargetY");
        flightTargetZ = compound.getDouble("FlightTargetZ");
        dungBallUUID = compound.hasUUID("DungBall") ? compound.getUUID("DungBall") : null;
    }
}
