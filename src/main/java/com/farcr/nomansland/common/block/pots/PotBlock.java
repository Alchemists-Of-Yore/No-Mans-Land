package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public class PotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PotBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    public MapCodec<PotBlock> codec() {
        return simpleCodec(PotBlock::new);
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection()).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

//    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
//        BlockEntity blockEntity = level.getBlockEntity(pos);
//        if (blockEntity instanceof PotBlockEntity decoratedpotblockentity) {
//            if (level.isClientSide) {
//                return ItemInteractionResult.CONSUME;
//            } else {
//                ItemStack itemstack1 = decoratedpotblockentity.getTheItem();
//                if (!stack.isEmpty() && (itemstack1.isEmpty() || ItemStack.isSameItemSameComponents(itemstack1, stack) && itemstack1.getCount() < itemstack1.getMaxStackSize())) {
//                    decoratedpotblockentity.wobble(PotBlockEntity.WobbleStyle.POSITIVE);
//                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
//                    ItemStack itemstack = stack.consumeAndReturn(1, player);
//                    float f;
//                    if (decoratedpotblockentity.isEmpty()) {
//                        decoratedpotblockentity.setTheItem(itemstack);
//                        f = (float)itemstack.getCount() / (float)itemstack.getMaxStackSize();
//                    } else {
//                        itemstack1.grow(1);
//                        f = (float)itemstack1.getCount() / (float)itemstack1.getMaxStackSize();
//                    }
//
//                    level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1, 0.7F + 0.5F * f);
//                    if (level instanceof ServerLevel) {
//                        ServerLevel serverlevel = (ServerLevel)level;
//                        serverlevel.sendParticles(ParticleTypes.DUST_PLUME, (double)pos.getX() + (double)0.5F, (double)pos.getY() + 1.2, (double)pos.getZ() + (double)0.5F, 7, (double)0, (double)0, (double)0, (double)0);
//                    }
//
//                    decoratedpotblockentity.setChanged();
//                    level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
//                    return ItemInteractionResult.SUCCESS;
//                } else {
//                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
//                }
//            }
//        } else {
//            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
//        }
//    }
//
//    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
//        BlockEntity blockEntity = level.getBlockEntity(pos);
//        if (blockEntity instanceof PotBlockEntity decoratedpotblockentity) {
//            level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1, 1);
//            decoratedpotblockentity.wobble(PotBlockEntity.WobbleStyle.NEGATIVE);
//            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
//            return InteractionResult.SUCCESS;
//        } else {
//            return InteractionResult.PASS;
//        }
//    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null) return pot.variant.shape();
        return Shapes.block();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, WATERLOGGED);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            if (pot.variant == null) {
                List<Holder.Reference<PotVariant>> variants = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).holders().filter(variant -> variant.value().size() == pot.size).toList();
                pot.variant = variants.get(level.getRandom().nextInt(variants.size())).value();
            }
        }
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected SoundType getSoundType(BlockState p_277561_) {
        // TODO: SoundType.DECORATED_POT_CRACKED when brittle
        return SoundType.DECORATED_POT;
    }

    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockpos = hit.getBlockPos();
        if (!level.isClientSide && projectile.mayInteract(level, blockpos) && projectile.mayBreak(level)) {
            level.destroyBlock(blockpos, true, projectile);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    //    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
//        BlockEntity blockEntity = level.getBlockEntity(pos);
//        ItemStack var10000;
//        if (blockEntity instanceof PotBlockEntity decoratedpotblockentity) {
//            var10000 = decoratedpotblockentity.getPotAsItem();
//        } else {
//            var10000 = super.getCloneItemStack(level, pos, state);
//        }
//
//        return var10000;
//    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
    }
}
