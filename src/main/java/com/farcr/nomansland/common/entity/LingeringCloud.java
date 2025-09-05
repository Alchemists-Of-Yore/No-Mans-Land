package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LingeringCloud extends Entity implements TraceableEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int TIME_BETWEEN_APPLICATIONS = 5;

    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(LingeringCloud.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_WAITING = SynchedEntityData.defineId(LingeringCloud.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ParticleOptions> DATA_PARTICLE = SynchedEntityData.defineId(LingeringCloud.class, EntityDataSerializers.PARTICLE);

    private static final float MAX_RADIUS = 32.0F;
    private static final float MINIMAL_RADIUS = 0.5F;
    private static final float DEFAULT_RADIUS = 3.0F;
    public static final float DEFAULT_WIDTH = 6.0F;
    public static final float HEIGHT = 0.5F;
    private PotionContents potionContents;
    protected final Map<LivingEntity, Integer> victims;
    private int duration;
    private int waitTime;
    private int reapplicationDelay;
    private int durationOnUse;
    private float radiusOnUse;
    private float radiusPerTick;
    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public LingeringCloud(EntityType<? extends LingeringCloud> entityType, Level level) {
        super(entityType, level);
        potionContents = PotionContents.EMPTY;
        victims = Maps.newHashMap();
        duration = 600;
        waitTime = 20;
        reapplicationDelay = 20;
        noPhysics = true;
    }

    public LingeringCloud(Level level, double x, double y, double z) {
        this(NMLEntities.LINGERING_CLOUD.get(), level);
        setPos(x, y, z);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_RADIUS, 3.0F);
        builder.define(DATA_WAITING, false);
        builder.define(DATA_PARTICLE, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1));
    }

    public void setRadius(float radius) {
        if (!level().isClientSide) {
            getEntityData().set(DATA_RADIUS, Mth.clamp(radius, 0.0F, 32.0F));
        }
    }

    public void refreshDimensions() {
        double d0 = getX();
        double d1 = getY();
        double d2 = getZ();
        super.refreshDimensions();
        setPos(d0, d1, d2);
    }

    public float getRadius() {
        return getEntityData().get(DATA_RADIUS);
    }

    public void setPotionContents(PotionContents potionContents) {
        this.potionContents = potionContents;
        updateColor();
    }

    private void updateColor() {
        ParticleOptions particleoptions = entityData.get(DATA_PARTICLE);
        if (particleoptions instanceof ColorParticleOption colorparticleoption) {
            int i = potionContents.equals(PotionContents.EMPTY) ? 0 : potionContents.getColor();
            entityData.set(DATA_PARTICLE, ColorParticleOption.create(colorparticleoption.getType(), FastColor.ARGB32.opaque(i)));
        }
    }

    @Override
    public void tick() {
        super.tick();

        Level level = level();
        boolean isWaiting = isWaiting();
        float radius = getRadius();
        double centerY = getY() + getBbHeight() / 2.0;

        if (level.isClientSide) {
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

                if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
                    if (isWaiting && random.nextBoolean()) {
                        level.addAlwaysVisibleParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1), x, y, z, 0, 0, 0);
                    } else {
                        level.addAlwaysVisibleParticle(particle, x, y, z, 0, 0, 0);
                    }
                } else if (isWaiting) {
                    level.addAlwaysVisibleParticle(particle, x, y, z, 0, 0, 0);
                } else {
                    level.addAlwaysVisibleParticle(particle, x, y, z, (0.5 - random.nextDouble()) * 0.15, 0.01, (0.5 - random.nextDouble()) * 0.15);
                }
            }
        } else {
            if (tickCount >= waitTime + duration) {
                discard();
                return;
            }

            boolean shouldBeWaiting = tickCount < waitTime;
            if (isWaiting != shouldBeWaiting) setWaiting(shouldBeWaiting);
            if (shouldBeWaiting) return;

            if (radiusPerTick != 0.0F) {
                radius += radiusPerTick;
                if (radius < 0.2F) {
                    discard();
                    return;
                }
                setRadius(radius);
            }

            if (tickCount % 5 == 0) {
                victims.entrySet().removeIf(entry -> tickCount >= entry.getValue());

                if (!potionContents.hasEffects()) {
                    victims.clear();
                } else {
                    List<MobEffectInstance> effects = new ArrayList<>();
                    if (potionContents.potion().isPresent()) {
                        for (MobEffectInstance effect : ((Potion) ((Holder<?>) potionContents.potion().get()).value()).getEffects()) {
                            effects.add(new MobEffectInstance(effect.getEffect(), effect.mapDuration(d -> d / 4), effect.getAmplifier(), effect.isAmbient(), effect.isVisible()));
                        }
                    }
                    effects.addAll(potionContents.customEffects());

                    AABB area = new AABB(
                            getX() - radius, centerY - radius, getZ() - radius,
                            getX() + radius, centerY + radius, getZ() + radius
                    );
                    List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area);

                    for (LivingEntity entity : entities) {
                        if (victims.containsKey(entity) || !entity.isAffectedByPotions()) continue;
                        if (effects.stream().noneMatch(entity::canBeAffected)) continue;

                        double dx = getX() - entity.getX();
                        double dy = centerY - entity.getY();
                        double dz = getZ() - entity.getZ();
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (distSq > radius * radius) continue;

                        victims.put(entity, tickCount + reapplicationDelay);
                        double distance = Math.sqrt(distSq);
                        double proximity = 1.0 - (distance / radius);
                        proximity = Mth.clamp(proximity, 0.0, 1.0);

                        for (MobEffectInstance effect : effects) {
                            if (effect.getEffect().value().isInstantenous()) {
                                effect.getEffect().value().applyInstantenousEffect(this, getOwner(), entity, effect.getAmplifier(), 0.5);
                            } else {
                                int scaledDuration = (int) Math.max(effect.getDuration() * 0.5, effect.getDuration() * proximity);
                                MobEffectInstance scaled = new MobEffectInstance(
                                        effect.getEffect(),
                                        scaledDuration,
                                        effect.getAmplifier(),
                                        effect.isAmbient(),
                                        effect.isVisible(),
                                        effect.showIcon()
                                );
                                entity.addEffect(scaled, this);
                            }
                        }

                        if (radiusOnUse != 0.0F) {
                            radius += radiusOnUse;
                            if (radius < 0.5F) {
                                discard();
                                return;
                            }
                            setRadius(radius);
                        }

                        if (durationOnUse != 0) {
                            duration += durationOnUse;
                            if (duration <= 0) {
                                discard();
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    public void addEffect(MobEffectInstance effectInstance) {
        setPotionContents(potionContents.withEffectAdded(effectInstance));
    }

    public ParticleOptions getParticle(LevelAccessor levelAccessor) {
        return getParticle();
    }

    public ParticleOptions getParticle() {
        return getEntityData().get(DATA_PARTICLE);
    }

    public void setParticle(ParticleOptions particleOption) {
        getEntityData().set(DATA_PARTICLE, particleOption);
    }

    protected void setWaiting(boolean waiting) {
        getEntityData().set(DATA_WAITING, waiting);
    }

    public boolean isWaiting() {
        return getEntityData().get(DATA_WAITING);
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public float getRadiusOnUse() {
        return radiusOnUse;
    }

    public void setRadiusOnUse(float radiusOnUse) {
        this.radiusOnUse = radiusOnUse;
    }

    public float getRadiusPerTick() {
        return radiusPerTick;
    }

    public void setRadiusPerTick(float radiusPerTick) {
        this.radiusPerTick = radiusPerTick;
    }

    public int getDurationOnUse() {
        return durationOnUse;
    }

    public void setDurationOnUse(int durationOnUse) {
        this.durationOnUse = durationOnUse;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.owner = owner;
        this.ownerUUID = owner == null ? null : owner.getUUID();
    }

    @Nullable
    public LivingEntity getOwner() {
        if (owner == null && ownerUUID != null && level() instanceof ServerLevel) {
            Entity entity = ((ServerLevel) level()).getEntity(ownerUUID);
            if (entity instanceof LivingEntity) {
                owner = (LivingEntity) entity;
            }
        }
        return owner;
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        tickCount = compound.getInt("Age");
        duration = compound.getInt("Duration");
        waitTime = compound.getInt("WaitTime");
        reapplicationDelay = compound.getInt("ReapplicationDelay");
        durationOnUse = compound.getInt("DurationOnUse");
        radiusOnUse = compound.getFloat("RadiusOnUse");
        radiusPerTick = compound.getFloat("RadiusPerTick");
        setRadius(compound.getFloat("Radius"));
        if (compound.hasUUID("Owner")) {
            ownerUUID = compound.getUUID("Owner");
        }

        RegistryOps<Tag> registryops = registryAccess().createSerializationContext(NbtOps.INSTANCE);
        if (compound.contains("Particle", 10)) {
            ParticleTypes.CODEC.parse(registryops, compound.get("Particle")).resultOrPartial(p -> {
                LOGGER.warn("Failed to parse area effect cloud particle options: '{}'", p);
            }).ifPresent(this::setParticle);
        }

        if (compound.contains("potion_contents")) {
            PotionContents.CODEC.parse(registryops, compound.get("potion_contents")).resultOrPartial(p -> {
                LOGGER.warn("Failed to parse area effect cloud potions: '{}'", p);
            }).ifPresent(this::setPotionContents);
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", tickCount);
        compound.putInt("Duration", duration);
        compound.putInt("WaitTime", waitTime);
        compound.putInt("ReapplicationDelay", reapplicationDelay);
        compound.putInt("DurationOnUse", durationOnUse);
        compound.putFloat("RadiusOnUse", radiusOnUse);
        compound.putFloat("RadiusPerTick", radiusPerTick);
        compound.putFloat("Radius", getRadius());
        RegistryOps<Tag> registryops = registryAccess().createSerializationContext(NbtOps.INSTANCE);
        compound.put("Particle", ParticleTypes.CODEC.encodeStart(registryops, getParticle()).getOrThrow());
        if (ownerUUID != null) {
            compound.putUUID("Owner", ownerUUID);
        }
        if (!potionContents.equals(PotionContents.EMPTY)) {
            Tag tag = PotionContents.CODEC.encodeStart(registryops, potionContents).getOrThrow();
            compound.put("potion_contents", tag);
        }
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_RADIUS.equals(key)) {
            refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.scalable(getRadius() * 2, getRadius() * 2);
    }
}