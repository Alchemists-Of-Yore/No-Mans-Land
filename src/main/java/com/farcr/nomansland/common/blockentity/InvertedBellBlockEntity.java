package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class InvertedBellBlockEntity extends BlockEntity {
    public static final int COOLDOWN = 100;

    public BlockPos targetBell;
    private BlockPos controller;
    private boolean active = false;
    private boolean valid = true;

    public int timer = 0;

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

        if (controller.level instanceof ServerLevel serverLevel) {
            if (teleport) {
                InvertedBellServerHandler.get(serverLevel).beginTeleport(serverLevel, controller.getBlockPos(), controller.targetBell);
            }
            controller.timer = COOLDOWN;
        } else {
            // todo proper sound
            this.level.playSound(Minecraft.getInstance().player, this.getBlockPos(), SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.0F, 1.0F);
            InvertedBellClientHandler.instance.onHit(hitDirection);
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
        if (ibbe.timer > 0) {
            ibbe.timer--;
        }
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
