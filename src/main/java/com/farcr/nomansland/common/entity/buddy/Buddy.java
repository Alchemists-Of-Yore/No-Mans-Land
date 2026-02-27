package com.farcr.nomansland.common.entity.buddy;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.List;

public class Buddy extends PathfinderMob implements Npc {
    public Buddy(EntityType<? extends Buddy> entityType, Level level) {
        super(entityType, level);
    }

    private BlockPos anchorPosition;
    public boolean isNaturallySpawned() {
        return (anchorPosition != null);
    }

    public static final List<String> COPY_ON_RESPAWN = List.of(
        "CustomName"
    );

    public void prepareAnchor(BlockPos anchorPosition) {
        this.anchorPosition = anchorPosition;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (anchorPosition != null)
            tag.put("BuddyAnchorPosition", NbtUtils.writeBlockPos(anchorPosition));
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        NbtUtils.readBlockPos(tag, "BuddyAnchorPosition").ifPresent(this::prepareAnchor);
        super.readAdditionalSaveData(tag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 50f);
    }

    public static boolean checkBuddySpawnRules(EntityType<? extends Buddy> animal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override public void remove(Entity.RemovalReason reason) {
        if (isNaturallySpawned() && this.level() instanceof ServerLevel serverLevel) {
            if (!serverLevel.isClientSide && (reason.shouldDestroy()))
                BuddyChunkAnchor.getOrDefault(serverLevel).queryRespawn(anchorPosition, this);
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }
}