package com.farcr.nomansland.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LadderBlock.class)
public abstract class LadderBlockMixin {

    @Shadow public abstract boolean canAttachTo(BlockGetter blockReader, BlockPos pos, Direction direction);

    @Inject(method = "canAttachTo", at = @At("HEAD"), cancellable = true)
    protected void nml$canAttachTo(BlockGetter blockReader, BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        BlockPos.MutableBlockPos mutableL = pos.relative(direction).mutable();
        BlockState blockState = blockReader.getBlockState(mutable);
        BlockState ladderState;

        if (blockState.isFaceSturdy(blockReader, mutable, direction) || blockState.isFaceSturdy(blockReader, mutable, Direction.UP)) {
            cir.setReturnValue(true);
            return;
        }

        while (mutable.getY() < blockReader.getMaxBuildHeight()) {
            mutable.move(Direction.UP);
            mutableL.move(Direction.UP);
            ladderState = blockReader.getBlockState(mutableL);
            if (!ladderState.hasProperty(LadderBlock.FACING) || ladderState.getValue(LadderBlock.FACING) != direction) {
                cir.setReturnValue(false);
                return;
            }
            if (canAttachTo(blockReader, mutable, direction)) {
                cir.setReturnValue(true);
                return;
            }
        }

        cir.setReturnValue(false);
    }

    @Inject(method = "updateShape", at = @At("HEAD"), cancellable = true)
    protected void nml$updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir) {
        if (!state.canSurvive(level, currentPos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
