package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

public class MoonCarvingBlock extends AncestralCarvingBlock {

    public MoonCarvingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return simpleCodec(MoonCarvingBlock::new);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos placedPos = context.getClickedPos();

        Direction facing;
        int rotation;
        float pitch = context.getPlayer() != null ? context.getPlayer().getXRot() : 0;
        if (pitch > 60) {
            facing = Direction.UP;
            rotation = getRotationForPlayer(context);
        } else if (pitch < -60) {
            facing = Direction.DOWN;
            rotation = getRotationForPlayer(context);
        } else {
            facing = context.getHorizontalDirection().getOpposite();
            rotation = 0;
        }

        Direction right = getPlaneRight(facing, rotation);
        Direction down = getPlaneDown(facing, rotation);

        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                if (col == 0 && row == 0) continue;
                BlockPos pos = placedPos.relative(right, col).relative(down, row);
                if (!level.getBlockState(pos).canBeReplaced(context)) {
                    return null;
                }
            }
        }

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(FORMATION, CarvingFormation.THREE_0_0)
                .setValue(ROTATION, rotation);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.isClientSide || oldState.is(this) || state.getValue(FORMATION) != CarvingFormation.THREE_0_0) return;

        Direction facing = state.getValue(FACING);
        int rotation = state.getValue(ROTATION);
        Direction right = getPlaneRight(facing, rotation);
        Direction down = getPlaneDown(facing, rotation);

        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                if (col == 0 && row == 0) continue;
                BlockPos target = pos.relative(right, col).relative(down, row);
                CarvingFormation formation = CarvingFormation.getForPosition(3, col, row);
                level.setBlock(target, this.defaultBlockState()
                        .setValue(FACING, facing)
                        .setValue(FORMATION, formation)
                        .setValue(ROTATION, rotation), 3);
            }
        }
    }
}
