package com.farcr.nomansland.common.entity.centipede;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import dev.tazer.mixed_litter.VariantUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Centipede extends Monster {
    public static final int MAX_SEGMENTS = 25;
    public static final int MAX_PIECES = MAX_SEGMENTS + 1;
    public static final int HEALTH_PER_SEGMENT = 2;
    public static final int SHRIVEL_DURATION = 40;
    public static final int SHRIVEL_COOLDOWN = 200;
    public static final double SEGMENT_SPACING = 0.375;

    private static final double CRUMB_MIN = 0.12;
    private static final double CRUMB_MAX = 0.5;
    private static final int TRACK_CAP = 160;
    private static final float NORMAL_EMA = 0.25F;
    private static final int LUNGE_TICKS = 8;
    private static final double LUNGE_SPEED = 0.55;
    public static final float GAIT_FREQ = 3.0F;
    public static final float WAVE_LAMBDA = 0.6F;

    private static final ResourceLocation NORMAL_VARIANT = NoMansLand.location("centipede/normal");
    private static final ResourceLocation VENOM_VARIANT = NoMansLand.location("centipede/venomous");

    private static final EntityDataAccessor<Integer> DATA_SEGMENTS = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_VENOMOUS = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> DATA_FLAGS = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DATA_BURROWED = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SHRIVELING = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_REARING = SynchedEntityData.defineId(Centipede.class, EntityDataSerializers.BOOLEAN);

    public static final float REAR_HEIGHT = 0.85F;
    private static final int REAR_SEGMENTS = 5;

    private final double[] pieceX = new double[MAX_PIECES];
    private final double[] pieceY = new double[MAX_PIECES];
    private final double[] pieceZ = new double[MAX_PIECES];
    private final double[] pieceXO = new double[MAX_PIECES];
    private final double[] pieceYO = new double[MAX_PIECES];
    private final double[] pieceZO = new double[MAX_PIECES];
    private final float[] pieceNX = new float[MAX_PIECES];
    private final float[] pieceNY = new float[MAX_PIECES];
    private final float[] pieceNZ = new float[MAX_PIECES];
    private final float[] pieceNXO = new float[MAX_PIECES];
    private final float[] pieceNYO = new float[MAX_PIECES];
    private final float[] pieceNZO = new float[MAX_PIECES];
    private final boolean[] pieceAir = new boolean[MAX_PIECES];

    private final double[] trackX = new double[TRACK_CAP];
    private final double[] trackY = new double[TRACK_CAP];
    private final double[] trackZ = new double[TRACK_CAP];
    private final double[] trackS = new double[TRACK_CAP];
    private final float[] trackNX = new float[TRACK_CAP];
    private final float[] trackNY = new float[TRACK_CAP];
    private final float[] trackNZ = new float[TRACK_CAP];
    private final boolean[] trackAir = new boolean[TRACK_CAP];
    private int trackHead = -1;
    private int trackCount = 0;
    private double headS;

    private float headNX = 0.0F;
    private float headNY = 1.0F;
    private float headNZ = 0.0F;
    private float headNXO = 0.0F;
    private float headNYO = 1.0F;
    private float headNZO = 0.0F;

    private double lastHeadX;
    private double lastHeadY;
    private double lastHeadZ;
    private double headGaitPhase;
    private double headGaitPhaseO;
    private float headSpeed;
    private boolean segmentsInitialized;
    private boolean variantApplied;

    private int rearTicks;
    private int jabCooldown;
    private float rearAmount;
    private float rearAmountO;
    private float shrivelAmount;
    private float shrivelAmountO;

    private int lungeTicks;
    private double lungeDirX;
    private double lungeDirY;
    private double lungeDirZ;

    private int shrivelTicks;
    private int shrivelCooldown;
    private int burrowCooldown;

    private final CentipedePart[] parts;

    public Centipede(EntityType<? extends Centipede> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
        this.moveControl = new CentipedeMoveControl(this);
        CentipedePart[] built = new CentipedePart[MAX_PIECES];
        for (int i = 0; i < built.length; i++) {
            built[i] = new CentipedePart(this, 0.7F, 0.55F);
        }
        this.parts = built;
        this.setId(ENTITY_COUNTER.getAndAdd(this.parts.length + 1) + 1);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.parts != null) {
            for (int i = 0; i < this.parts.length; i++) {
                this.parts[i].setId(id + i + 1);
            }
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.parts;
    }

    public boolean hurtPart(CentipedePart part, DamageSource source, float amount) {
        return this.hurt(source, amount);
    }

    @Override
    protected void doPush(Entity entity) {
        if (entity instanceof CentipedePart part && part.parent == this) {
            return;
        }
        super.doPush(entity);
    }

    @Override
    protected int calculateFallDamage(float fallDistance, float damageMultiplier) {
        return (int) (super.calculateFallDamage(fallDistance, damageMultiplier) * 0.8F);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(SEGMENT_SPACING * MAX_PIECES + 2.0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SEGMENTS, 5);
        builder.define(DATA_VENOMOUS, false);
        builder.define(DATA_FLAGS, (byte) 0);
        builder.define(DATA_BURROWED, false);
        builder.define(DATA_SHRIVELING, false);
        builder.define(DATA_REARING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new CentipedeShrivelGoal(this));
        this.goalSelector.addGoal(1, new CentipedeBurrowGoal(this));
        this.goalSelector.addGoal(3, new CentipedeAttackGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, Centipede::canTargetPlayer));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Animal.class, 10, true, false, Centipede::isPrey));
    }

    public static boolean shouldAttack(LivingEntity entity) {
        return isPrey(entity) && entity.isAlive();
    }

    public static boolean isPrey(LivingEntity entity) {
        return entity instanceof Animal && !(entity instanceof Centipede);
    }

    public static boolean canTargetPlayer(LivingEntity entity) {
        return entity instanceof Player player && !player.isCreative() && !player.isSpectator();
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte flags = this.entityData.get(DATA_FLAGS);
        flags = climbing ? (byte) (flags | 1) : (byte) (flags & ~1);
        this.entityData.set(DATA_FLAGS, flags);
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    public int getSegments() {
        return this.entityData.get(DATA_SEGMENTS);
    }

    public int getPieceCount() {
        return getSegments() + 1;
    }

    public void setSegments(int segments) {
        int clamped = Mth.clamp(segments, 3, MAX_SEGMENTS);
        this.entityData.set(DATA_SEGMENTS, clamped);
        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(clamped * HEALTH_PER_SEGMENT);
        }
    }

    public boolean isVenomous() {
        return this.entityData.get(DATA_VENOMOUS);
    }

    public void setVenomous(boolean venomous) {
        this.entityData.set(DATA_VENOMOUS, venomous);
    }

    public boolean isBurrowed() {
        return this.entityData.get(DATA_BURROWED);
    }

    public void setBurrowed(boolean burrowed) {
        this.entityData.set(DATA_BURROWED, burrowed);
    }

    public boolean isShriveling() {
        return this.entityData.get(DATA_SHRIVELING);
    }

    public void setShriveling(boolean shriveling) {
        this.entityData.set(DATA_SHRIVELING, shriveling);
    }

    public boolean canShrivel() {
        return this.shrivelCooldown <= 0 && !this.isShriveling();
    }

    public boolean isRearing() {
        return this.entityData.get(DATA_REARING);
    }

    public void setRearing(boolean rearing) {
        this.entityData.set(DATA_REARING, rearing);
    }

    public void startRear(int ticks) {
        if (this.headNY < 0.7F || isShriveling() || isBurrowed()) return;
        this.rearTicks = ticks;
        this.jabCooldown = 6;
        setRearing(true);
        this.getNavigation().stop();
    }

    public float getRearAmount(float partialTick) {
        return Mth.lerp(partialTick, this.rearAmountO, this.rearAmount);
    }

    public float getHeadRearLift(float partialTick) {
        return REAR_HEIGHT * getRearAmount(partialTick);
    }

    public double getPieceRenderX(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceXO[i], this.pieceX[i]);
    }

    public double getPieceRenderY(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceYO[i], this.pieceY[i]);
    }

    public double getPieceRenderZ(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceZO[i], this.pieceZ[i]);
    }

    public float getPieceRenderNX(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceNXO[i], this.pieceNX[i]);
    }

    public float getPieceRenderNY(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceNYO[i], this.pieceNY[i]);
    }

    public float getPieceRenderNZ(int i, float partialTick) {
        return Mth.lerp(partialTick, this.pieceNZO[i], this.pieceNZ[i]);
    }

    public float getHeadRenderNX(float partialTick) {
        return Mth.lerp(partialTick, this.headNXO, this.headNX);
    }

    public float getHeadRenderNY(float partialTick) {
        return Mth.lerp(partialTick, this.headNYO, this.headNY);
    }

    public float getHeadRenderNZ(float partialTick) {
        return Mth.lerp(partialTick, this.headNZO, this.headNZ);
    }

    public float getHeadGaitPhase(float partialTick) {
        return (float) Mth.lerp(partialTick, this.headGaitPhaseO, this.headGaitPhase);
    }

    public float getHeadSpeed() {
        return this.headSpeed;
    }

    public boolean isLunging() {
        return this.lungeTicks > 0;
    }

    public void requestLunge(double dx, double dy, double dz) {
        double l = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (l < 1.0E-6) return;
        this.lungeDirX = dx / l;
        this.lungeDirY = dy / l;
        this.lungeDirZ = dz / l;
        this.lungeTicks = LUNGE_TICKS;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource random = level.getRandom();
        boolean venomous = random.nextFloat() < 0.18F;
        setVenomous(venomous);
        int segments = venomous ? 3 + random.nextInt(5) : 5 + random.nextInt(6);
        setSegments(segments);
        setHealth(getMaxHealth());
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Segments", getSegments());
        compound.putBoolean("Venomous", isVenomous());
        compound.putInt("ShrivelCooldown", this.shrivelCooldown);
        compound.putInt("BurrowCooldown", this.burrowCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("Segments")) {
            setSegments(compound.getInt("Segments"));
        }
        setVenomous(compound.getBoolean("Venomous"));
        this.shrivelCooldown = compound.getInt("ShrivelCooldown");
        this.burrowCooldown = compound.getInt("BurrowCooldown");
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.setClimbing(this.horizontalCollision && !this.isBurrowed() && !this.isShriveling());
        }
        if (this.lungeTicks > 0) {
            boolean first = this.lungeTicks == LUNGE_TICKS;
            this.lungeTicks--;
            Vec3 dm = getDeltaMovement();
            double upY = first ? Math.max(dm.y, 0.42) : dm.y + this.lungeDirY * 0.05;
            setDeltaMovement(this.lungeDirX * LUNGE_SPEED, upY, this.lungeDirZ * LUNGE_SPEED);
            this.getNavigation().stop();
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (!this.variantApplied) {
            applyVariant();
            this.variantApplied = true;
        }
        if (this.rearTicks > 0) {
            this.rearTicks--;
            this.getNavigation().stop();
            setDeltaMovement(getDeltaMovement().multiply(0.6, 1.0, 0.6));
            if (this.jabCooldown > 0) this.jabCooldown--;
            LivingEntity target = getTarget();
            if (target != null && this.jabCooldown <= 0) {
                getLookControl().setLookAt(target, 40.0F, 40.0F);
                double reach = getBbWidth() * 2.0 * getBbWidth() * 2.0 + target.getBbWidth();
                if (distanceToSqr(target) <= reach + 1.5) {
                    doHurtTarget(target);
                    this.jabCooldown = 11;
                }
            }
            if (this.rearTicks == 0 || target == null) {
                this.rearTicks = 0;
                setRearing(false);
            }
        }
    }

    private void applyVariant() {
        if (this.level() instanceof ServerLevel serverLevel) {
            ResourceLocation id = isVenomous() ? VENOM_VARIANT : NORMAL_VARIANT;
            VariantUtil.setVariants(this, VariantUtil.lookupVariantIds(List.of(id), serverLevel.registryAccess()));
        }
    }

    @Override
    public void tick() {
        super.tick();
        updateTrail();
        updateParts();

        if (this.shrivelCooldown > 0) {
            this.shrivelCooldown--;
        }
        if (this.burrowCooldown > 0) {
            this.burrowCooldown--;
        }
        if (this.shrivelTicks > 0) {
            this.shrivelTicks--;
            if (this.shrivelTicks == 0 && !this.level().isClientSide) {
                setShriveling(false);
            }
        }
    }

    private void appendCrumb(double x, double y, double z, float nx, float ny, float nz, boolean air, double s) {
        this.trackHead = (this.trackHead + 1) % TRACK_CAP;
        this.trackX[this.trackHead] = x;
        this.trackY[this.trackHead] = y;
        this.trackZ[this.trackHead] = z;
        this.trackS[this.trackHead] = s;
        this.trackNX[this.trackHead] = nx;
        this.trackNY[this.trackHead] = ny;
        this.trackNZ[this.trackHead] = nz;
        this.trackAir[this.trackHead] = air;
        if (this.trackCount < TRACK_CAP) {
            this.trackCount++;
        }
    }

    private void seedTrack() {
        double hx = getX();
        double hy = getY();
        double hz = getZ();
        Vec3 n = computeNormal(hx, hy + 0.1, hz);
        this.headNX = this.headNXO = (float) n.x;
        this.headNY = this.headNYO = (float) n.y;
        this.headNZ = this.headNZO = (float) n.z;
        float rad = this.yBodyRot * Mth.DEG_TO_RAD;
        double bx = Mth.sin(rad) * CRUMB_MIN;
        double bz = -Mth.cos(rad) * CRUMB_MIN;
        this.trackHead = -1;
        this.trackCount = 0;
        double px = hx - bx * (TRACK_CAP - 1);
        double pz = hz - bz * (TRACK_CAP - 1);
        double s = 0.0;
        for (int k = 0; k < TRACK_CAP; k++) {
            appendCrumb(px, hy, pz, this.headNX, this.headNY, this.headNZ, false, s);
            s += CRUMB_MIN;
            px += bx;
            pz += bz;
        }
        this.headS = this.trackS[this.trackHead];
        this.lastHeadX = hx;
        this.lastHeadY = hy;
        this.lastHeadZ = hz;
        this.headGaitPhase = 0.0;
        this.headGaitPhaseO = 0.0;
        this.headSpeed = 0.0F;

        double sbx = Mth.sin(rad) * SEGMENT_SPACING;
        double sbz = -Mth.cos(rad) * SEGMENT_SPACING;
        for (int i = 0; i < MAX_PIECES; i++) {
            this.pieceX[i] = this.pieceXO[i] = hx + sbx * (i + 1);
            this.pieceY[i] = this.pieceYO[i] = hy;
            this.pieceZ[i] = this.pieceZO[i] = hz + sbz * (i + 1);
            this.pieceNX[i] = this.pieceNXO[i] = this.headNX;
            this.pieceNY[i] = this.pieceNYO[i] = this.headNY;
            this.pieceNZ[i] = this.pieceNZO[i] = this.headNZ;
            this.pieceAir[i] = false;
        }
        this.segmentsInitialized = true;
    }

    private void updateTrail() {
        if (!this.segmentsInitialized) {
            seedTrack();
            return;
        }

        int count = getPieceCount();
        boolean burrowed = isBurrowed();
        double hx = getX();
        double hy = getY();
        double hz = getZ();

        for (int i = 0; i < MAX_PIECES; i++) {
            this.pieceXO[i] = this.pieceX[i];
            this.pieceYO[i] = this.pieceY[i];
            this.pieceZO[i] = this.pieceZ[i];
            this.pieceNXO[i] = this.pieceNX[i];
            this.pieceNYO[i] = this.pieceNY[i];
            this.pieceNZO[i] = this.pieceNZ[i];
        }
        this.headNXO = this.headNX;
        this.headNYO = this.headNY;
        this.headNZO = this.headNZ;
        this.headGaitPhaseO = this.headGaitPhase;
        this.rearAmountO = this.rearAmount;
        this.shrivelAmountO = this.shrivelAmount;

        double disp = Math.sqrt(sq(hx - this.lastHeadX) + sq(hy - this.lastHeadY) + sq(hz - this.lastHeadZ));
        this.headGaitPhase += disp;
        this.headSpeed = Mth.lerp(0.3F, this.headSpeed, (float) disp);
        this.lastHeadX = hx;
        this.lastHeadY = hy;
        this.lastHeadZ = hz;

        Vec3 raw = computeNormal(hx, hy + 0.1, hz);
        this.headNX = Mth.lerp(NORMAL_EMA, this.headNX, (float) raw.x);
        this.headNY = Mth.lerp(NORMAL_EMA, this.headNY, (float) raw.y);
        this.headNZ = Mth.lerp(NORMAL_EMA, this.headNZ, (float) raw.z);
        float hnl = (float) Math.sqrt(this.headNX * this.headNX + this.headNY * this.headNY + this.headNZ * this.headNZ);
        if (hnl > 1.0E-5F) {
            this.headNX /= hnl;
            this.headNY /= hnl;
            this.headNZ /= hnl;
        } else {
            this.headNX = 0.0F;
            this.headNY = 1.0F;
            this.headNZ = 0.0F;
        }

        double newX = this.trackX[this.trackHead];
        double newY = this.trackY[this.trackHead];
        double newZ = this.trackZ[this.trackHead];
        double nd = Math.sqrt(sq(hx - newX) + sq(hy - newY) + sq(hz - newZ));
        if (nd >= CRUMB_MIN) {
            boolean air = !hasNearbySolid(hx - this.headNX * 0.5, hy - this.headNY * 0.5, hz - this.headNZ * 0.5);
            if (nd > CRUMB_MAX) {
                int nsub = (int) Math.ceil(nd / CRUMB_MIN);
                double px = newX;
                double py = newY;
                double pz = newZ;
                for (int k = 1; k <= nsub; k++) {
                    double t = (double) k / nsub;
                    double cx = Mth.lerp(t, newX, hx);
                    double cy = Mth.lerp(t, newY, hy);
                    double cz = Mth.lerp(t, newZ, hz);
                    this.headS += Math.sqrt(sq(cx - px) + sq(cy - py) + sq(cz - pz));
                    appendCrumb(cx, cy, cz, this.headNX, this.headNY, this.headNZ, air, this.headS);
                    px = cx;
                    py = cy;
                    pz = cz;
                }
            } else {
                this.headS += nd;
                appendCrumb(hx, hy, hz, this.headNX, this.headNY, this.headNZ, air, this.headS);
            }
            newX = this.trackX[this.trackHead];
            newY = this.trackY[this.trackHead];
            newZ = this.trackZ[this.trackHead];
        }

        double dHead = Math.sqrt(sq(hx - newX) + sq(hy - newY) + sq(hz - newZ));
        double virtHeadS = this.headS + dHead;
        int tail = (this.trackHead - this.trackCount + 1 + TRACK_CAP) % TRACK_CAP;
        int cursor = this.trackHead;

        for (int i = 0; i < count; i++) {
            double pathS = virtHeadS - (i + 1) * SEGMENT_SPACING;
            double rx;
            double ry;
            double rz;
            float rnx;
            float rny;
            float rnz;
            boolean rair;
            if (pathS >= this.headS) {
                double t = dHead < 1.0E-6 ? 0.0 : (pathS - this.headS) / dHead;
                t = Mth.clamp(t, 0.0, 1.0);
                rx = Mth.lerp(t, newX, hx);
                ry = Mth.lerp(t, newY, hy);
                rz = Mth.lerp(t, newZ, hz);
                rnx = Mth.lerp((float) t, this.trackNX[this.trackHead], this.headNX);
                rny = Mth.lerp((float) t, this.trackNY[this.trackHead], this.headNY);
                rnz = Mth.lerp((float) t, this.trackNZ[this.trackHead], this.headNZ);
                rair = false;
            } else {
                while (cursor != tail && this.trackS[cursor] > pathS) {
                    cursor = (cursor - 1 + TRACK_CAP) % TRACK_CAP;
                }
                if (this.trackS[cursor] > pathS) {
                    rx = this.trackX[cursor];
                    ry = this.trackY[cursor];
                    rz = this.trackZ[cursor];
                    rnx = this.trackNX[cursor];
                    rny = this.trackNY[cursor];
                    rnz = this.trackNZ[cursor];
                    rair = this.trackAir[cursor];
                } else {
                    int hi = (cursor + 1) % TRACK_CAP;
                    double span = this.trackS[hi] - this.trackS[cursor];
                    double t = span < 1.0E-9 ? 0.0 : (pathS - this.trackS[cursor]) / span;
                    t = Mth.clamp(t, 0.0, 1.0);
                    rx = Mth.lerp(t, this.trackX[cursor], this.trackX[hi]);
                    ry = Mth.lerp(t, this.trackY[cursor], this.trackY[hi]);
                    rz = Mth.lerp(t, this.trackZ[cursor], this.trackZ[hi]);
                    rnx = Mth.lerp((float) t, this.trackNX[cursor], this.trackNX[hi]);
                    rny = Mth.lerp((float) t, this.trackNY[cursor], this.trackNY[hi]);
                    rnz = Mth.lerp((float) t, this.trackNZ[cursor], this.trackNZ[hi]);
                    rair = this.trackAir[cursor] && this.trackAir[hi];
                }
            }
            this.pieceX[i] = rx;
            this.pieceY[i] = ry;
            this.pieceZ[i] = rz;
            float nl = (float) Math.sqrt(rnx * rnx + rny * rny + rnz * rnz);
            if (nl > 1.0E-5F) {
                this.pieceNX[i] = rnx / nl;
                this.pieceNY[i] = rny / nl;
                this.pieceNZ[i] = rnz / nl;
            } else {
                this.pieceNX[i] = 0.0F;
                this.pieceNY[i] = 1.0F;
                this.pieceNZ[i] = 0.0F;
            }
            this.pieceAir[i] = rair;
        }

        float rearTarget = isRearing() ? 1.0F : 0.0F;
        this.rearAmount = Mth.lerp(0.18F, this.rearAmount, rearTarget);
        if (this.rearAmount > 0.01F) {
            for (int i = 0; i < count && i < REAR_SEGMENTS; i++) {
                float t = (float) (REAR_SEGMENTS - i) / (REAR_SEGMENTS + 1);
                double lift = REAR_HEIGHT * t * t * this.rearAmount;
                this.pieceX[i] += this.headNX * lift;
                this.pieceY[i] += this.headNY * lift;
                this.pieceZ[i] += this.headNZ * lift;
            }
        }

        float shrivelTarget = isShriveling() ? 1.0F : 0.0F;
        this.shrivelAmount = Mth.lerp(0.15F, this.shrivelAmount, shrivelTarget);
        if (this.shrivelAmount > 0.01F) {
            double cx = getX();
            double cy = getY();
            double cz = getZ();
            float time = this.tickCount * 0.45F;
            float ang = this.yBodyRot * Mth.DEG_TO_RAD;
            double px = cx;
            double pz = cz;
            for (int i = 0; i < count; i++) {
                float writhe = Mth.sin(time + i * 0.7F) * 0.4F;
                ang += 0.55F + writhe;
                px += Mth.sin(ang) * SEGMENT_SPACING;
                pz += -Mth.cos(ang) * SEGMENT_SPACING;
                double vy = cy + Mth.sin(time * 1.4F + i * 0.5F) * 0.12F;
                float a = this.shrivelAmount;
                this.pieceX[i] = Mth.lerp(a, this.pieceX[i], px);
                this.pieceY[i] = Mth.lerp(a, this.pieceY[i], vy);
                this.pieceZ[i] = Mth.lerp(a, this.pieceZ[i], pz);
            }
        }

        if (!burrowed) for (int i = 0; i < count; i++) {
            if (isSolidAt(this.pieceX[i], this.pieceY[i], this.pieceZ[i])) {
                for (int s = 0; s < 6; s++) {
                    this.pieceX[i] += this.pieceNX[i] * 0.05;
                    this.pieceY[i] += this.pieceNY[i] * 0.05;
                    this.pieceZ[i] += this.pieceNZ[i] * 0.05;
                    if (!isSolidAt(this.pieceX[i], this.pieceY[i], this.pieceZ[i])) {
                        break;
                    }
                }
            }
        }

        double tx = count > 0 ? this.pieceX[count - 1] : hx;
        double ty = count > 0 ? this.pieceY[count - 1] : hy;
        double tz = count > 0 ? this.pieceZ[count - 1] : hz;
        for (int i = count; i < MAX_PIECES; i++) {
            this.pieceX[i] = this.pieceXO[i] = tx;
            this.pieceY[i] = this.pieceYO[i] = ty;
            this.pieceZ[i] = this.pieceZO[i] = tz;
            this.pieceNX[i] = 0.0F;
            this.pieceNY[i] = 1.0F;
            this.pieceNZ[i] = 0.0F;
            this.pieceAir[i] = false;
        }

        if (!Double.isFinite(this.pieceX[0]) || !Double.isFinite(this.pieceY[0]) || !Double.isFinite(this.pieceZ[0])) {
            seedTrack();
        }
    }

    private static double sq(double a) {
        return a * a;
    }

    private Vec3 computeNormal(double x, double y, double z) {
        BlockPos base = BlockPos.containing(x, y, z);
        double nx = 0.0;
        double ny = 0.0;
        double nz = 0.0;
        for (Direction dir : Direction.values()) {
            BlockPos b = base.relative(dir);
            if (!this.level().getBlockState(b).getCollisionShape(this.level(), b).isEmpty()) {
                nx -= dir.getStepX();
                ny -= dir.getStepY();
                nz -= dir.getStepZ();
            }
        }
        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len < 1.0E-6) {
            return new Vec3(0.0, 1.0, 0.0);
        }
        return new Vec3(nx / len, ny / len, nz / len);
    }

    private boolean hasNearbySolid(double x, double y, double z) {
        BlockPos base = BlockPos.containing(x, y, z);
        if (!this.level().getBlockState(base).getCollisionShape(this.level(), base).isEmpty()) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            BlockPos b = base.relative(dir);
            if (!this.level().getBlockState(b).getCollisionShape(this.level(), b).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private boolean isSolidAt(double x, double y, double z) {
        BlockPos bp = BlockPos.containing(x, y, z);
        return !this.level().getBlockState(bp).getCollisionShape(this.level(), bp).isEmpty();
    }

    private void updateParts() {
        int count = getPieceCount();
        for (int i = 0; i < this.parts.length; i++) {
            CentipedePart part = this.parts[i];
            part.xOld = part.getX();
            part.yOld = part.getY();
            part.zOld = part.getZ();
            part.xo = part.getX();
            part.yo = part.getY();
            part.zo = part.getZ();
            if (i >= count) {
                part.setPos(getX(), getY(), getZ());
            } else {
                part.setPos(this.pieceX[i], this.pieceY[i] - 0.2, this.pieceZ[i]);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean success = super.doHurtTarget(target);
        if (success && isVenomous() && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0), this);
        }
        return success;
    }

    @Override
    public boolean killedEntity(ServerLevel level, LivingEntity entity) {
        if (isPrey(entity)) {
            entity.skipDropExperience();
            entity.nml$skipDroppingDeathLoot();
            if (!entity.isBaby()) eatAndGrow();
        }
        return super.killedEntity(level, entity);
    }

    private void eatAndGrow() {
        if (getSegments() < MAX_SEGMENTS) {
            setSegments(getSegments() + 1);
        }
        setHealth(getMaxHealth());
        this.playSound(NMLSounds.CENTIPEDE_HISS.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.is(DamageTypeTags.IS_FIRE) && canShrivel()) {
            startShrivel();
        }
        return hurt;
    }

    public void startShrivel() {
        this.shrivelTicks = SHRIVEL_DURATION;
        this.shrivelCooldown = SHRIVEL_COOLDOWN;
        setShriveling(true);
        setClimbing(false);
        setBurrowed(false);
        setBurrowPhysics(false);
        this.rearTicks = 0;
        setRearing(false);
        this.getNavigation().stop();
        this.setTarget(null);
    }

    public boolean isInShrivelState() {
        return this.shrivelTicks > 0;
    }

    public boolean canBurrowNow() {
        return this.burrowCooldown <= 0;
    }

    public void setBurrowPhysics(boolean active) {
        this.noPhysics = active;
        if (active) {
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    public boolean isBurrowBlock(BlockPos pos) {
        return this.level().getBlockState(pos).is(NMLTags.CENTIPEDE_BURROW);
    }

    public boolean isPassableAt(BlockPos pos) {
        return this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty();
    }

    public void resetBurrowCooldown() {
        this.burrowCooldown = 600;
    }

    public boolean isOnBurrowBlock() {
        BlockState below = this.level().getBlockState(this.blockPosition().below());
        BlockState at = this.level().getBlockState(this.blockPosition());
        return below.is(NMLTags.CENTIPEDE_BURROW) || at.is(NMLTags.CENTIPEDE_BURROW);
    }

    @Nullable
    public BlockPos findNearbyBurrow() {
        BlockPos origin = this.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-6, -2, -6), origin.offset(6, 2, 6))) {
            if (this.level().getBlockState(pos).is(NMLTags.CENTIPEDE_BURROW)
                    && this.level().getBlockState(pos.above()).getCollisionShape(this.level(), pos.above()).isEmpty()) {
                return pos.above().immutable();
            }
        }
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.CENTIPEDE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.CENTIPEDE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.CENTIPEDE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(NMLSounds.CENTIPEDE_STEP.get(), 0.12F, 0.8F + this.random.nextFloat() * 0.3F);
    }

    public static boolean checkCentipedeSpawnRules(EntityType<Centipede> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return true;
    }
}
