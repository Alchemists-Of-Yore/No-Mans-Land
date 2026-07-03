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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
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
    private int harassAnimationTick = 0;
    private int grabAnimationTick = 0;
    public final AnimationState hurtingAnimationState = new AnimationState();
    public final AnimationState fallingAnimationState = new AnimationState();
    public final AnimationState intimidatingAnimationState = new AnimationState();
    public final AnimationState harassAnimationState = new AnimationState();
    public final AnimationState chestStealAnimationState = new AnimationState();
    public final AnimationState grabAnimationState = new AnimationState();
    public final AnimationState flyingAnimationState = new AnimationState();

    private static final double FLOCK_RADIUS = 12.0;
    private static final int FLAP_DISPLAY_TICKS = 12;
    private static final long ATTACK_TARGET_EXPIRY = 240L;
    private static final byte EVENT_HARASS = 61;
    private static final byte EVENT_GRAB = 62;
    private static final int HARASS_ANIMATION_TICKS = 15;
    private static final int GRAB_ANIMATION_TICKS = 50;
    private static final int HONK_COOLDOWN_TICKS = 15;
    private static final int FLIGHT_POSE_DWELL_TICKS = 4;

    private final GooseGrudges grudges = new GooseGrudges();
    @Nullable
    private BlockPos aggressionAnchor;
    private long attackReadyAt;
    private int flapTicks;
    private boolean stealing;
    private boolean flying;
    private boolean rummaging;
    private long lastFlightControlTime;
    private int honkCooldown;
    private double lastTickX;
    private double lastTickZ;
    private int stationaryTicks;
    private static final int RUNNING_GRACE_TICKS = 3;
    private static final double MOVING_THRESHOLD_SQR = 2.5E-4;
    private FlightPose flightPose = FlightPose.FORWARD;
    private FlightPose pendingFlightPose = FlightPose.FORWARD;
    private int pendingFlightPoseTicks;
    private float flightRoll;
    private float flightRollO;
    private float flightPitch;
    private float flightPitchO;
    private boolean migrating;
    private boolean arriving;
    private boolean migrationTransit;
    private int formationIndex;
    @Nullable
    private Goose flockLeader;
    @Nullable
    private Vec3 migrationHeading;
    private double migrationCeiling;
    @Nullable
    private BlockPos landingSpot;
    @Nullable
    private Vec3 flightInvite;
    private long flightInviteReady;
    private long flightInviteExpiry;

    public Goose(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        setPathfindingMalus(PathType.WATER, 0.0F);
        setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
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
        setNoGravity(flying);
        if (flying) markFlightControl();
    }

    public void markFlightControl() {
        if (!level().isClientSide) lastFlightControlTime = level().getGameTime();
    }

    public FlightPose getFlightPose() {
        return flightPose;
    }

    public float getFlightRoll(float partialTick) {
        return Mth.lerp(partialTick, flightRollO, flightRoll);
    }

    public float getFlightPitch(float partialTick) {
        return Mth.lerp(partialTick, flightPitchO, flightPitch);
    }

    public boolean isMigrating() {
        return migrating;
    }

    public boolean isArriving() {
        return arriving;
    }

    public boolean isTransit() {
        return migrationTransit;
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
        migrationTransit = false;
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
        migrationTransit = false;
        flockLeader = leader;
        formationIndex = index;
        migrationHeading = heading;
        migrationCeiling = 0;
        landingSpot = landing;
        setFlying(true);
    }

    public void startTransit(@Nullable Goose leader, int index, Vec3 heading) {
        migrating = true;
        arriving = false;
        migrationTransit = true;
        flockLeader = leader;
        formationIndex = index;
        migrationHeading = heading;
        migrationCeiling = getY();
        landingSpot = null;
        setFlying(true);
        getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        getNavigation().stop();
    }

    public void inviteFlight(Vec3 destination) {
        flightInvite = destination;
        long now = level().getGameTime();
        flightInviteReady = now + random.nextInt(12);
        flightInviteExpiry = now + 100L;
    }

    @Nullable
    public Vec3 pendingFlightInvite() {
        if (flightInvite == null) return null;
        long now = level().getGameTime();
        if (now > flightInviteExpiry) {
            flightInvite = null;
            return null;
        }
        return now >= flightInviteReady ? flightInvite : null;
    }

    public void clearFlightInvite() {
        flightInvite = null;
    }

    public void finishMigrationFlight() {
        migrating = false;
        arriving = false;
        migrationTransit = false;
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
        if (!level().isClientSide) level().broadcastEntityEvent(this, EVENT_HARASS);
    }

    public void playGrabAnimation() {
        if (!level().isClientSide) level().broadcastEntityEvent(this, EVENT_GRAB);
    }

    public boolean isRummaging() {
        return level().isClientSide ? getState() == State.RUMMAGING : rummaging;
    }

    public void setRummaging(boolean rummaging) {
        this.rummaging = rummaging;
    }

    public void honk() {
        voice(NMLSounds.GOOSE_AMBIENT.get());
    }

    public void honkAngry() {
        voice(NMLSounds.GOOSE_ANGRY.get());
    }

    public void honkAfraid() {
        voice(NMLSounds.GOOSE_AFRAID.get());
    }

    public void honkCurious() {
        voice(NMLSounds.GOOSE_CURIOUS.get());
    }

    private void voice(SoundEvent sound) {
        if (honkCooldown > 0) return;
        honkCooldown = HONK_COOLDOWN_TICKS;
        makeSound(sound);
    }

    public void dropCarriedItem() {
        ItemStack carried = getCarriedItem();
        if (!carried.isEmpty()) {
            if (!level().isClientSide) {
                ItemEntity dropped = spawnAtLocation(carried);
                if (dropped != null) {
                    dropped.setUnlimitedLifetime();
                    dropped.setNoPickUpDelay();
                    dropped.getPersistentData().putBoolean("GooseDropped", true);
                }
            }
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

    public void faceToward(double x, double z) {
        double dx = x - getX();
        double dz = z - getZ();
        if (dx * dx + dz * dz < 1.0E-4) return;
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        setYRot(yaw);
        yBodyRot = yaw;
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
        honkAngry();
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
        if (migrating && !migrationTransit) {
            CompoundTag mig = new CompoundTag();
            mig.putBoolean("Arriving", arriving);
            mig.putInt("FormationIndex", formationIndex);
            if (migrationHeading != null) {
                mig.putDouble("HeadingX", migrationHeading.x);
                mig.putDouble("HeadingZ", migrationHeading.z);
            }
            mig.putDouble("Ceiling", migrationCeiling);
            if (landingSpot != null) mig.putLong("Landing", landingSpot.asLong());
            compound.put("Migration", mig);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        grudges.load(compound);
        setCarriedItem(compound.contains("CarriedItem")
                ? ItemStack.parseOptional(registryAccess(), compound.getCompound("CarriedItem"))
                : ItemStack.EMPTY);
        if (compound.contains("Migration")) {
            CompoundTag mig = compound.getCompound("Migration");
            migrating = true;
            migrationTransit = false;
            arriving = mig.getBoolean("Arriving");
            formationIndex = mig.getInt("FormationIndex");
            migrationHeading = mig.contains("HeadingX")
                    ? new Vec3(mig.getDouble("HeadingX"), 0, mig.getDouble("HeadingZ")) : null;
            migrationCeiling = mig.getDouble("Ceiling");
            landingSpot = mig.contains("Landing") ? BlockPos.of(mig.getLong("Landing")) : null;
            flockLeader = null;
            setFlying(true);
        }
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
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
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
        Brain<Goose> brain = getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return NMLSounds.GOOSE_ANGRY.get();
        }
        if (brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET) && !canFight()) {
            return NMLSounds.GOOSE_AFRAID.get();
        }
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
        playSound(NMLSounds.GOOSE_STEP.get(), 0.15F, 0.9F + random.nextFloat() * 0.2F);
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
            setFlying(false);
        }
        if (!flying && isNoGravity()) {
            setNoGravity(false);
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
            setState(isRunningPose() ? State.RUNNING : State.IDLING);
        } else if (brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            if (canFight()) {
                setState(State.INTIMIDATING);
            } else {
                setState(isRunningPose() ? State.RUNNING : State.IDLING);
            }
        } else if (rummaging) {
            setState(State.RUMMAGING);
        } else if (isCarrying()) {
            setState(isRunningPose() ? State.RUNNING : State.IDLING);
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

        if (!level().isClientSide && flying && !onGround() && tickCount % 11 == 0) {
            playSound(SoundEvents.PARROT_FLY, 0.25F, 0.55F + random.nextFloat() * 0.1F);
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
    public void travel(Vec3 travelVector) {
        if (isFlying() && !isInWater() && !onGround()) {
            move(MoverType.SELF, getDeltaMovement());
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            hurtingAnimationState.start(tickCount);
            hurtAnimationTick = 22;
        }

        if (id == EVENT_HARASS) {
            harassAnimationState.start(tickCount);
            harassAnimationTick = HARASS_ANIMATION_TICKS;
        }

        if (id == EVENT_GRAB) {
            grabAnimationState.start(tickCount);
            grabAnimationTick = GRAB_ANIMATION_TICKS;
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

        if (harassAnimationTick > 0) harassAnimationTick--;
        else harassAnimationState.ifStarted(AnimationState::stop);

        if (grabAnimationTick > 0) grabAnimationTick--;
        else grabAnimationState.ifStarted(AnimationState::stop);

        if (level().isClientSide) {
            flightRollO = flightRoll;
            flightPitchO = flightPitch;
            float targetRoll = inFlight ? Mth.clamp(-Mth.degreesDifference(yRotO, getYRot()) * 4.0F, -35.0F, 35.0F) : 0.0F;
            float targetPitch = inFlight ? (float) Mth.clamp(-(getY() - yo) * 28.0, -32.0, 32.0) : 0.0F;
            flightRoll += (targetRoll - flightRoll) * 0.25F;
            flightPitch += (targetPitch - flightPitch) * 0.25F;

            if (inFlight) {
                flyingAnimationState.startIfStopped(tickCount);
                updateFlightPose();
            } else {
                flyingAnimationState.stop();
                flightPose = FlightPose.FORWARD;
            }

            if (getState() == State.INTIMIDATING) intimidatingAnimationState.startIfStopped(tickCount);
            else intimidatingAnimationState.stop();

            if (getState() == State.RUMMAGING) chestStealAnimationState.startIfStopped(tickCount);
            else chestStealAnimationState.stop();
        }

        if (!level().isClientSide) {
            if (flapTicks > 0) flapTicks--;
            if (honkCooldown > 0) honkCooldown--;
            double dx = getX() - lastTickX;
            double dz = getZ() - lastTickZ;
            if (dx * dx + dz * dz > MOVING_THRESHOLD_SQR) stationaryTicks = 0;
            else stationaryTicks++;
            lastTickX = getX();
            lastTickZ = getZ();
        }

        floatGoose();

        if (isInWater() && !isFlying()) {
            float maxBodyTurn = 6.0F;
            float bodyDiff = Mth.wrapDegrees(yBodyRot - yBodyRotO);
            if (Math.abs(bodyDiff) > maxBodyTurn) yBodyRot = yBodyRotO + Math.copySign(maxBodyTurn, bodyDiff);
        }
    }

    private boolean isRunningPose() {
        return stationaryTicks <= RUNNING_GRACE_TICKS;
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
        GoosePathNavigation navigation = new GoosePathNavigation(this, level);
        navigation.setCanFloat(true);
        return navigation;
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
        RUMMAGING(4);

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
