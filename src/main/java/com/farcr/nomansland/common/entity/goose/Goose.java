package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataSerializers;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntFunction;

public class Goose extends Animal {

    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(Goose.class, NMLEntityDataSerializers.GOOSE_STATE.get());
    private static final EntityDataAccessor<ItemStack> DATA_CARRIED_ITEM = SynchedEntityData.defineId(Goose.class, EntityDataSerializers.ITEM_STACK);
    private int hurtAnimationTick = 0;
    private int peckAnimationTick = 0;
    public final AnimationState hurtingAnimationState = new AnimationState();
    public final AnimationState fallingAnimationState = new AnimationState();
    public final AnimationState intimidatingAnimationState = new AnimationState();
    public final AnimationState peckingAnimationState = new AnimationState();
    public final AnimationState flyingAnimationState = new AnimationState();
    public final AnimationState drinkingAnimationState = new AnimationState();

    private static final double FLOCK_RADIUS = 12.0;
    private static final int FLAP_DISPLAY_TICKS = 12;
    private static final long ATTACK_TARGET_EXPIRY = 240L;
    private static final byte EVENT_PECK = 61;
    private static final int PECK_ANIMATION_TICKS = 10;
    private static final int HONK_COOLDOWN_TICKS = 15;
    private static final int FLIGHT_POSE_DWELL_TICKS = 4;

    private final GooseGrudges grudges = new GooseGrudges();
    @Nullable
    private BlockPos aggressionAnchor;
    private long attackReadyAt;
    private int flapTicks;
    private boolean stealing;
    private boolean flying;
    private boolean drinking;
    private long lastFlightControlTime;
    private int honkCooldown;
    private FlightPose flightPose = FlightPose.FORWARD;
    private FlightPose pendingFlightPose = FlightPose.FORWARD;
    private int pendingFlightPoseTicks;
    private boolean migrating;
    private boolean arriving;
    private int formationIndex;
    @Nullable
    private Goose flockLeader;
    @Nullable
    private Vec3 migrationHeading;
    private double migrationCeiling;
    @Nullable
    private BlockPos landingSpot;

    public Goose(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public GooseGrudges getGrudges() {
        return grudges;
    }

    @Nullable
    public BlockPos getAggressionAnchor() {
        return aggressionAnchor;
    }

    public void setAnchor(BlockPos pos) {
        aggressionAnchor = pos;
    }

    public ItemStack getCarriedItem() {
        return entityData.get(DATA_CARRIED_ITEM);
    }

    public void setCarriedItem(ItemStack stack) {
        entityData.set(DATA_CARRIED_ITEM, stack);
    }

    public boolean isCarrying() {
        return !getCarriedItem().isEmpty();
    }

    public boolean isStealing() {
        return stealing;
    }

    public void setStealing(boolean stealing) {
        this.stealing = stealing;
    }

    public boolean isFlying() {
        return level().isClientSide ? getState() == State.FLYING : flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
        if (flying) markFlightControl();
    }

    public void markFlightControl() {
        if (!level().isClientSide) lastFlightControlTime = level().getGameTime();
    }

    public boolean isDrinking() {
        return level().isClientSide ? getState() == State.DRINKING : drinking;
    }

    public void setDrinking(boolean drinking) {
        this.drinking = drinking;
    }

    public FlightPose getFlightPose() {
        return flightPose;
    }

    public boolean isMigrating() {
        return migrating;
    }

    public boolean isArriving() {
        return arriving;
    }

    public int getFormationIndex() {
        return formationIndex;
    }

    @Nullable
    public Goose getFlockLeader() {
        return flockLeader;
    }

    @Nullable
    public Vec3 getMigrationHeading() {
        return migrationHeading;
    }

    public double getMigrationCeiling() {
        return migrationCeiling;
    }

    @Nullable
    public BlockPos getLandingSpot() {
        return landingSpot;
    }

    public void startMigration(@Nullable Goose leader, int index, Vec3 heading, double ceiling) {
        migrating = true;
        arriving = false;
        flockLeader = leader;
        formationIndex = index;
        migrationHeading = heading;
        migrationCeiling = ceiling;
        landingSpot = null;
        setFlying(true);
        getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        getNavigation().stop();
        honk();
    }

    public void startArrival(@Nullable Goose leader, int index, Vec3 heading, BlockPos landing) {
        migrating = true;
        arriving = true;
        flockLeader = leader;
        formationIndex = index;
        migrationHeading = heading;
        migrationCeiling = 0;
        landingSpot = landing;
        setFlying(true);
    }

    public void finishMigrationFlight() {
        migrating = false;
        arriving = false;
        flockLeader = null;
        migrationHeading = null;
        landingSpot = null;
        setFlying(false);
    }

    public static float weaponBonus(ItemStack stack) {
        float[] bonus = {0};
        stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers().forEach(entry -> {
            if (entry.slot().test(EquipmentSlot.MAINHAND)
                    && entry.attribute().is(Attributes.ATTACK_DAMAGE.unwrapKey().orElseThrow())
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                bonus[0] += (float) entry.modifier().amount();
            }
        });
        return Math.max(0, bonus[0]);
    }

    public boolean isArmed() {
        return isCarrying() && weaponBonus(getCarriedItem()) > 0;
    }

    @Nullable
    public ItemEntity findNearbyWeapon(double radius) {
        ItemEntity closest = null;
        double best = Double.MAX_VALUE;
        for (ItemEntity item : level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(radius))) {
            if (weaponBonus(item.getItem()) <= 0) continue;
            double distance = distanceToSqr(item);
            if (distance < best) {
                best = distance;
                closest = item;
            }
        }
        return closest;
    }

    public void grabItem(ItemEntity item) {
        setCarriedItem(item.getItem().copyWithCount(1));
        item.getItem().shrink(1);
        if (item.getItem().isEmpty()) item.discard();
    }

    public void peck() {
        if (!level().isClientSide) level().broadcastEntityEvent(this, EVENT_PECK);
    }

    public void honk() {
        if (honkCooldown > 0) return;
        honkCooldown = HONK_COOLDOWN_TICKS;
        makeSound(NMLSounds.GOOSE_AMBIENT.get());
    }

    public void dropCarriedItem() {
        ItemStack carried = getCarriedItem();
        if (!carried.isEmpty()) {
            if (!level().isClientSide) spawnAtLocation(carried);
            setCarriedItem(ItemStack.EMPTY);
        }
    }

    public boolean isAttackReady() {
        return level().getGameTime() >= attackReadyAt;
    }

    public void setAttackCooldown(int ticks) {
        attackReadyAt = level().getGameTime() + ticks;
    }

    public void flapBriefly() {
        flapTicks = FLAP_DISPLAY_TICKS;
    }

    public List<Goose> nearbyGeese(double radius) {
        return level().getEntitiesOfClass(Goose.class, getBoundingBox().inflate(radius), other -> other != this && other.isAlive());
    }

    public int flockConfidence() {
        return nearbyGeese(FLOCK_RADIUS).size();
    }

    public void beginAttack(LivingEntity target) {
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) return;
        aggressionAnchor = blockPosition();
        Brain<Goose> brain = getBrain();
        brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, target, ATTACK_TARGET_EXPIRY);
        setTarget(target);
        honk();
    }

    public void rallyFlock(LivingEntity target) {
        for (Goose ally : nearbyGeese(FLOCK_RADIUS)) {
            if (ally.canFight() && (!ally.isCarrying() || ally.isArmed()) && !ally.isStealing() && !ally.isFlying() && !ally.isMigrating()
                    && ally.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()) {
                ally.beginAttack(target);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    public static boolean checkGooseSpawnRules(EntityType<? extends Animal> animal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        boolean flag = isBrightEnoughToSpawn(level, pos);
        return level.getBlockState(pos.above()).isAir() && flag;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return NMLEntities.GOOSE.get().create(serverLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLING);
        builder.define(DATA_CARRIED_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        grudges.save(compound);
        if (isCarrying()) {
            compound.put("CarriedItem", getCarriedItem().save(registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        grudges.load(compound);
        setCarriedItem(compound.contains("CarriedItem")
                ? ItemStack.parseOptional(registryAccess(), compound.getCompound("CarriedItem"))
                : ItemStack.EMPTY);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        dropCarriedItem();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason.shouldDestroy()) dropCarriedItem();
        super.remove(reason);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.PUMPKIN_SEEDS);
    }

    @Override
    public void setInLove(@Nullable Player player) {
        super.setInLove(player);

        if (player != null) {
            getBrain().getMemory(MemoryModuleType.ANGRY_AT).ifPresent(angryAt -> {
                if (angryAt.equals(player.getUUID())) {
                    getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
                }
            });

            getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                if (target == player) {
                    getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                    setTarget(null);
                }
            });
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_STATE.equals(key)) {
            resetAnimations();
            if (getState() == State.INTIMIDATING) {
                intimidatingAnimationState.startIfStopped(tickCount);
            }
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    private void resetAnimations() {
        intimidatingAnimationState.stop();
    }

    public State getState() {
        return entityData.get(DATA_STATE);
    }

    public boolean showWings() {
        State state = getState();
        return state == State.INTIMIDATING || state == State.RUNNING || state == State.FLYING
                || hurtingAnimationState.isStarted() || fallingAnimationState.isStarted();
    }

    public void setState(State state) {
        entityData.set(DATA_STATE, state);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected Brain.Provider<Goose> brainProvider() {
        return GooseAI.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return GooseAI.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<Goose> getBrain() {
        return (Brain<Goose>) super.getBrain();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.GOOSE_AMBIENT.get();
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.GOOSE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.GOOSE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        playSound(NMLSounds.GOOSE_STEP.get(), 0.15F, 1);
    }

    public boolean canFight() {
        return !isBaby() && getHealth() > getMaxHealth() / 2;
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) level();

        level.getProfiler().push("gooseBrain");
        getBrain().tick(level, this);
        level.getProfiler().pop();

        if (flying && level.getGameTime() - lastFlightControlTime > 4) {
            flying = false;
        }

        GooseAI.updateActivity(this);
        updateState();

        super.customServerAiStep();
    }

    private void updateState() {
        Brain<Goose> brain = getBrain();
        if (flying) {
            setState(State.FLYING);
        } else if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            setState(brain.hasMemoryValue(MemoryModuleType.WALK_TARGET) ? State.RUNNING : State.IDLING);
        } else if (brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            if (canFight()) {
                setState(State.INTIMIDATING);
            } else {
                setState(brain.hasMemoryValue(MemoryModuleType.WALK_TARGET) ? State.RUNNING : State.IDLING);
            }
        } else if (isCarrying()) {
            setState(brain.hasMemoryValue(MemoryModuleType.WALK_TARGET) ? State.RUNNING : State.IDLING);
        } else if (drinking) {
            setState(State.DRINKING);
        } else if (flapTicks > 0) {
            setState(State.INTIMIDATING);
        } else {
            setState(State.IDLING);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level().isClientSide && tickCount % 200 == 0 && getHealth() < getMaxHealth()) {
            heal(1.0F);
        }

        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0 && !isFlying()) {
            this.setDeltaMovement(vec3.multiply(1, 0.6, 1));
        }
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            hurtingAnimationState.start(tickCount);
            hurtAnimationTick = 22;
        }

        if (id == EVENT_PECK) {
            peckingAnimationState.start(tickCount);
            peckAnimationTick = PECK_ANIMATION_TICKS;
        }

        super.handleEntityEvent(id);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        level().broadcastEntityEvent(this, (byte) 4);
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        boolean inFlight = getState() == State.FLYING;
        if (inFlight || onGround() || getDeltaMovement().y > 0) fallingAnimationState.ifStarted(AnimationState::stop);
        else fallingAnimationState.startIfStopped(tickCount);

        if (hurtAnimationTick > 0) hurtAnimationTick--;
        else hurtingAnimationState.ifStarted(AnimationState::stop);

        if (peckAnimationTick > 0) peckAnimationTick--;
        else peckingAnimationState.ifStarted(AnimationState::stop);

        if (level().isClientSide) {
            if (inFlight) {
                flyingAnimationState.startIfStopped(tickCount);
                updateFlightPose();
            } else {
                flyingAnimationState.stop();
                flightPose = FlightPose.FORWARD;
            }

            if (getState() == State.DRINKING) drinkingAnimationState.startIfStopped(tickCount);
            else drinkingAnimationState.stop();
        }

        if (!level().isClientSide) {
            if (flapTicks > 0) flapTicks--;
            if (honkCooldown > 0) honkCooldown--;
        }

        floatGoose();
    }

    private void updateFlightPose() {
        double dy = getY() - yo;
        FlightPose observed = flightPose;
        if (dy > 0.1) observed = FlightPose.ASCENDING;
        else if (dy < -0.06) observed = FlightPose.GLIDING;
        else if (dy > -0.02 && dy < 0.07) observed = FlightPose.FORWARD;

        if (observed == flightPose) {
            pendingFlightPoseTicks = 0;
        } else if (observed == pendingFlightPose) {
            if (++pendingFlightPoseTicks >= FLIGHT_POSE_DWELL_TICKS) {
                flightPose = observed;
                pendingFlightPoseTicks = 0;
            }
        } else {
            pendingFlightPose = observed;
            pendingFlightPoseTicks = 1;
        }
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getFluidState(pos).is(FluidTags.WATER) ? 5 : level.getPathfindingCostFromLightLevels(pos);
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    private void floatGoose() {
        if (isInWater() && !isFlying()) {
            CollisionContext collisioncontext = CollisionContext.of(this);
            if (collisioncontext.isAbove(LiquidBlock.STABLE_SHAPE, blockPosition(), true) && !level().getFluidState(blockPosition().above()).is(FluidTags.WATER)) {
                if (random.nextFloat() < 0.2F) setDeltaMovement(getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
                else setOnGround(true);
            } else {
                setDeltaMovement(getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
            }
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GoosePathNavigation(this, level);
    }

    public static class GoosePathNavigation extends GroundPathNavigation {
        GoosePathNavigation(Goose goose, Level level) {
            super(goose, level);
        }

        protected PathFinder createPathFinder(int maxVisitedNodes) {
            nodeEvaluator = new WalkNodeEvaluator();
            nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(nodeEvaluator, maxVisitedNodes);
        }

        protected boolean hasValidPathType(PathType pathType) {
            return pathType == PathType.WATER || super.hasValidPathType(pathType);
        }

        public boolean isStableDestination(BlockPos pos) {
            return level.getBlockState(pos).is(Blocks.WATER) || super.isStableDestination(pos);
        }
    }

    public enum FlightPose {
        ASCENDING,
        FORWARD,
        GLIDING
    }

    public enum State {
        IDLING(0),
        INTIMIDATING(1),
        RUNNING(2),
        FLYING(3),
        DRINKING(4);

        public static final IntFunction<State> BY_ID = ByIdMap.continuous(State::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, State::id);
        private final int id;

        State(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }
}
