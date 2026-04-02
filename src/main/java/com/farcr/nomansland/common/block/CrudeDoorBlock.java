package com.farcr.nomansland.common.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import javax.annotation.Nullable;

public class CrudeDoorBlock extends DoorBlock {
    public static final MapCodec<CrudeDoorBlock> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(BlockSetType.CODEC.fieldOf("block_set_type").forGetter(CrudeDoorBlock::type), propertiesCodec())
                    .apply(instance, CrudeDoorBlock::new)
    );

    public CrudeDoorBlock(BlockSetType blockSetType, Properties properties) {
        super(blockSetType, properties);
    }

    @Override
    public MapCodec<? extends DoorBlock> codec() {
        return CODEC;
    }

    private Direction getHingeDirection(BlockState state) {
        Direction facing = state.getValue(FACING);
        return state.getValue(HINGE) == DoorHingeSide.LEFT
                ? facing.getCounterClockWise()
                : facing.getClockWise();
    }

    private boolean hasHingeSupport(LevelReader level, BlockPos pos, Direction hingeDir) {
        BlockPos supportPos = pos.relative(hingeDir);
        BlockState supportState = level.getBlockState(supportPos);
        return supportState.isFaceSturdy(level, supportPos, hingeDir.getOpposite());
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction hingeDir = getHingeDirection(state);
         return hasHingeSupport(level, pos, hingeDir);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        Direction hingeDir = getHingeDirection(state);
        BlockPos pos = context.getClickedPos();
        if (!hasHingeSupport(context.getLevel(), pos, hingeDir) || !hasHingeSupport(context.getLevel(), pos.above(), hingeDir)) {
            return null;
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)) {
            return neighborState.getBlock() instanceof DoorBlock && neighborState.getValue(HALF) != half
                    ? neighborState.setValue(HALF, half)
                    : Blocks.AIR.defaultBlockState();
        }
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
