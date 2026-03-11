package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;

public class InvertedBellBlockEntity extends BlockEntity {
    public BlockPos targetBell;
    private BlockPos controller;
    private boolean active = false;
    private boolean valid = true;

    private float animationTimer = 0;
    private Direction direction;

    public InvertedBellBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.INVERTED_BELL.get(), pos, blockState);
    }

    public boolean isController() {
        return this.getBlockState().getValue(InvertedBellBlock.CONTROLLER);
    }

    public boolean canSurvive() {
        InvertedBellBlockEntity controller = this.getController();
        if (controller != null) {
            BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    for (int y = -1; y < 2; y++) {
                        mutPos.setWithOffset(controller.getBlockPos(), x, y, z);
                        if (!controller.level.getBlockState(mutPos).is(NMLBlocks.INVERTED_BELL.block())) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    public void destroyBell() {
        if (!this.valid) {
            return;
        }

        InvertedBellBlockEntity controller = this.getController();
        if (controller != null) {
            BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    for (int y = -1; y < 2; y++) {
                        if (controller.level.getBlockEntity(mutPos.setWithOffset(controller.getBlockPos(), x, y, z))
                                instanceof InvertedBellBlockEntity ibbe) {
                            ibbe.valid = false; // prevent cascading block updates
                        }
                    }
                }
            }

            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    for (int y = -1; y < 2; y++) {
                        if (controller.level.getBlockState(mutPos.setWithOffset(controller.getBlockPos(), x, y, z))
                                .is(NMLBlocks.INVERTED_BELL.block())) {
                            controller.level.destroyBlock(mutPos, false);
                        }
                    }
                }
            }
        }
    }

    public void ring(Direction hitDirection, boolean teleport) {
        InvertedBellBlockEntity controller = this.getController();
        if (controller == null) {
            return;
        }

        if (controller.level.isClientSide) {
            controller.direction = hitDirection;
            controller.animationTimer = ANIMATION_DURATION;
        } else if (teleport) {
            controller.tryTransportEntities(hitDirection);
        }
    }

    public void tryTransportEntities(Direction hitDirection) {
        if (this.targetBell == null) {
            return;
        }
        if (!(this.level.getBlockEntity(this.targetBell) instanceof InvertedBellBlockEntity other) ||
                other.targetBell != this.getBlockPos()) {
            this.targetBell = null;
            return;
        }

        this.level.blockEvent(this.targetBell, NMLBlocks.INVERTED_BELL.block(), 2, hitDirection.get2DDataValue());

        List<Entity> entities = this.level.getEntities(null, new AABB(this.getBlockPos()).inflate(16));
        for (Entity entity : entities) {
            if (entity.distanceToSqr(this.getBlockPos().getCenter()) < 16*16) {
                Vec3 diff = entity.position().subtract(this.getBlockPos().getCenter());
                Vec3 newPos = this.targetBell.getCenter().add(diff);
                entity.teleportTo(newPos.x, newPos.y, newPos.z);
            }
        }
    }

    public @Nullable InvertedBellBlockEntity getController() {
        if (this.controller != null) {
            BlockEntity be = this.level.getBlockEntity(this.controller);
            if (be instanceof InvertedBellBlockEntity ibbe && ibbe.isController()) {
                return ibbe;
            }
        }

        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    mutPos.setWithOffset(this.getBlockPos(), x, y, z);
                    BlockEntity be = this.level.getBlockEntity(mutPos);
                    if (be instanceof InvertedBellBlockEntity ibbe && ibbe.isController()) {
                        this.controller = mutPos;
                        return ibbe;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public boolean triggerEvent(final int id, final int type) {
        if (id == 1) {
            this.ring(Direction.from2DDataValue(type), true);
            return true;
        } else if (id == 2) {
            this.ring(Direction.from2DDataValue(type), false);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InvertedBellBlockEntity ibbe) {
        if (ibbe.animationTimer > 0) {
            ibbe.animationTimer--;
        }
    }

    private static final int ANIMATION_DURATION = 100;
    private static final double ANIMATION_DECAY = 0.7;
    private static final double ANIMATION_INTENSITY = 0.25;
    private static final double ANIMATION_SPEED = 0.25;
    private float getAnimationAngle(float pt) {
        float t = this.animationTimer - pt;
        if (this.animationTimer > 0) {
            double decay = (Math.exp(t / ANIMATION_DURATION) - 1) / (Math.exp(ANIMATION_DECAY) - 1);
            double wobble = Math.sin((ANIMATION_DURATION - t) * ANIMATION_SPEED) * ANIMATION_INTENSITY;
            return (float) (decay * wobble);
        }
        return 0;
    }

    public @Nullable Quaternionf getAnimationRotation(float pt) {
        if (this.direction != null) {
            return (switch (this.direction) {
                case NORTH -> Axis.XN;
                case SOUTH -> Axis.XP;
                case EAST -> Axis.ZN;
                case WEST -> Axis.ZP;
                default -> throw new IllegalStateException("Unexpected value: " + this.direction);
            }).rotation(this.getAnimationAngle(pt));
        }
        return null;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.targetBell != null) {
            tag.putInt("targetX", this.targetBell.getX());
            tag.putInt("targetY", this.targetBell.getY());
            tag.putInt("targetZ", this.targetBell.getZ());
        }
        tag.putBoolean("active", this.active);
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("targetX")) {
            this.targetBell = new BlockPos(
                    tag.getInt("targetX"),
                    tag.getInt("targetY"),
                    tag.getInt("targetZ")
            );
        }
        this.active = tag.getBoolean("active");
    }
}
