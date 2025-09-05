package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class Ember extends Entity implements TraceableEntity {

    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUUID;

    public Ember(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public Ember(Level level, double x, double y, double z) {
        super(NMLEntities.EMBER.get(), level);
        setPos(x, y, z);
        setNoGravity(false);
    }

    @Override
    public void tick() {
        super.tick();

        if (!onGround()) {
            Vec3 motion = getDeltaMovement();
            double dy = Mth.clamp(motion.y - 0.02, -0.15, 0);
            setDeltaMovement(motion.x, dy, motion.z);
            move(MoverType.SELF, getDeltaMovement());
        }

        if (level().isClientSide && level().random.nextFloat() < 0.8f) {
            level().addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(),
                    (random.nextDouble() - 0.5) * 0.02,
                    0.02 + random.nextDouble() * 0.02,
                    (random.nextDouble() - 0.5) * 0.02);
        }

        if (onGround() && !level().isClientSide) {
            BlockPos pos = blockPosition();

            if (BaseFireBlock.canBePlacedAt(level(), pos, Direction.UP)) {
                level().setBlockAndUpdate(pos, BaseFireBlock.getState(level(), pos));
            }

            discard();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.15;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    public void setOwner(@javax.annotation.Nullable LivingEntity owner) {
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
        if (compound.hasUUID("Owner")) {
            ownerUUID = compound.getUUID("Owner");
        }
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("Age", tickCount);
        if (ownerUUID != null) {
            compound.putUUID("Owner", ownerUUID);
        }
    }
}
