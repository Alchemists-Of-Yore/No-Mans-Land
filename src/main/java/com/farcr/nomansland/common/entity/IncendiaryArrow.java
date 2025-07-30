package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public class IncendiaryArrow extends AbstractArrow {
    public IncendiaryArrow(EntityType<? extends IncendiaryArrow> entityType, Level level) {
        super(entityType, level);
    }

    public IncendiaryArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(NMLEntities.INCENDIARY_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
        setRemainingFireTicks(600);
    }

    public IncendiaryArrow(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(NMLEntities.INCENDIARY_ARROW.get(), owner, level, pickupItemStack, firedFromWeapon);
        setRemainingFireTicks(600);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity entity = result.getEntity();
        entity.igniteForSeconds(30);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        Direction direction = result.getDirection();
        BlockPos pos = result.getBlockPos();
        Level level = level();
        BlockState state = level.getBlockState(pos);
        BlockPos neighbourPos = pos.relative(direction);

        if (level instanceof ServerLevel serverLevel) {
            if (level.getBlockState(neighbourPos).is(Blocks.FIRE)) {
                BlockPos.withinManhattan(neighbourPos, 2, 0, 2).forEach(firePos -> {
                    if ((BaseFireBlock.canBePlacedAt(level, firePos, direction) || level.getBlockState(firePos).isFlammable(level, firePos, direction)) && level.random.nextFloat() < 0.2)
                        level.setBlockAndUpdate(firePos, BaseFireBlock.getState(level, firePos));
                });

                level.playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1, 1);
                for (int i = 0; i < 20; i++) {
                    double velX = (random.nextDouble() - 0.5) * 0.15;
                    double velY = 0.05 + random.nextDouble() * 0.1;
                    double velZ = (random.nextDouble() - 0.5) * 0.15;

                    serverLevel.sendParticles(ParticleTypes.FLAME, position().x, position().y, position().z, 3, velX, velY, velZ, 0.2);
                }
            } else if (state.isFlammable(level, pos, direction) && (BaseFireBlock.canBePlacedAt(level, neighbourPos, direction) || level.getBlockState(neighbourPos).isFlammable(level, neighbourPos, direction))) {
                level.playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1, 1);
                level.setBlockAndUpdate(neighbourPos, BaseFireBlock.getState(level, neighbourPos));
            } else {
                clearFire();
            }

            if (isOnFire()) {
                discard();
                serverLevel.sendParticles(ParticleTypes.SMOKE, position().x, position().y, position().z, 3, 0, 0, 0, 0.01);
            }
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return NMLItems.INCENDIARY_ARROW.stack();
    }

    @Override
    protected ItemStack getPickupItem() {
        return isOnFire() ? getDefaultPickupItem() : new ItemStack(Items.ARROW);
    }
}
