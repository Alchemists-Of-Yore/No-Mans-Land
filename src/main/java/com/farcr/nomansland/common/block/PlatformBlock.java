package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class PlatformBlock extends Block implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final EnumProperty<Direction.Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty UNSTABLE = BlockStateProperties.UNSTABLE;
    protected static final VoxelShape TOP_SHAPE = Block.box(0.0D, 12.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape BOTTOM_SHAPE = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 8.0D, 16.0D);

    public PlatformBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false).setValue(UP, false).setValue(UNSTABLE, false));
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(UP)) {
            return TOP_SHAPE;
        }
        return BOTTOM_SHAPE;
    }

    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility.equals(ItemAbilities.SHEARS_TRIM) && !state.getValue(UNSTABLE)) {
            context.getLevel().playSound(null, context.getClickedPos(), NMLSounds.WOODEN_PLATFORM_CRACKS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            return state.setValue(UNSTABLE, true);
        }
        return null;
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState()
                .setValue(UP, Mth.frac(context.getClickLocation().y)>0.5)
                .setValue(HORIZONTAL_AXIS, context.getHorizontalDirection().getAxis())
                .setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity.onGround() && state.getValue(UNSTABLE)) {
            level.destroyBlock(pos, false, entity);
            level.playSound(null, pos, NMLSounds.WOODEN_PLATFORM_BREAKS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, WATERLOGGED, HORIZONTAL_AXIS, UNSTABLE);
    }
}

