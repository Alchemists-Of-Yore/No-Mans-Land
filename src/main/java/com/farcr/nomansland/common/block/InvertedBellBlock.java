package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class InvertedBellBlock extends BaseEntityBlock {
    public static DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static int CONTROLLER_PART = 3 * 3 * 3 / 2;
    public static IntegerProperty PART = IntegerProperty.create("part", 0, 3 * 3 * 3 - 1);

    public InvertedBellBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HORIZONTAL_FACING, Direction.NORTH)
                .setValue(PART, 0)
        );
    }

    public static final VoxelShape[] BELL_NS = new VoxelShape[3 * 3 * 3];
    public static final VoxelShape[] BELL_EW = new VoxelShape[3 * 3 * 3];

    static {
        final VoxelShape lowerRun = Block.box(-10, -16, -10, 26, -14, 26);
        final VoxelShape mainBody = Block.box(-8, -14, -8, 24, 28, 24);
        final VoxelShape beamNS = Block.box(6, 28, -16, 10, 32, 32);
        final VoxelShape beamEW = Block.box(-16, 28, 6, 32, 32, 10);

        final VoxelShape fullBellNS = Shapes.or(lowerRun, mainBody, beamNS);
        final VoxelShape fullBellEW = Shapes.or(lowerRun, mainBody, beamEW);
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    final int i = x + z * 3 + y * 9 + 13;
                    final VoxelShape here = Block.box(x * 16, y * 16, z * 16, x * 16 + 16, y * 16 + 16, z * 16 + 16);
                    BELL_NS[i] = Shapes.join(fullBellNS, here, BooleanOp.AND).move(-x, -y, -z);
                    BELL_EW[i] = Shapes.join(fullBellEW, here, BooleanOp.AND).move(-x, -y, -z);
                }
            }
        }
    }

    @Override
    protected VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        if (state.getValue(HORIZONTAL_FACING).getAxis() == Direction.Axis.X) {
            return BELL_NS[state.getValue(PART)];
        } else {
            return BELL_EW[state.getValue(PART)];
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        final BlockPos blockpos = context.getClickedPos();
        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        final Level level = context.getLevel();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
                    mutPos.setWithOffset(blockpos, x, y, z);
                    if (!level.getBlockState(mutPos).canBeReplaced(context)) {
                        return null;
                    }
                }
            }
        }
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection());
    }

    public static void placeBell(final BlockPos bottomCenter, final Direction facing, final LevelWriter level) {
        final BlockState baseState = NMLBlocks.INVERTED_BELL.get().defaultBlockState().setValue(HORIZONTAL_FACING, facing);
        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
                    mutPos.setWithOffset(bottomCenter, x, y, z);
                    final int i = x + z * 3 + y * 9 + 4;
                    level.setBlock(mutPos, baseState.setValue(PART, i), 3);
                }
            }
        }
    }

    @Override
    public void setPlacedBy(final Level level, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack) {
        placeBell(pos, state.getValue(HORIZONTAL_FACING), level);
    }

    private boolean onHit(final Level level, final BlockState state, final BlockPos pos, final Direction direction) {
        final Direction front = state.getValue(HORIZONTAL_FACING);
        if (!level.isClientSide && direction.getAxis() == front.getAxis()) {
            final InvertedBellBlockEntity controller = getControllerBE(level, pos, state);
            if (controller != null && controller.ringCooldown <= 0) {
                level.blockEvent(controller.getBlockPos(), controller.getBlockState().getBlock(), 1,
                        direction == front ? 2 : 0 // types are an unsigned byte...
                );
                return true;
            }
        }

        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
        if (player.getMainHandItem().isEmpty()) {
            if (this.onHit(level, state, pos, hitResult.getDirection())) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void onProjectileHit(final Level level, final BlockState state, final BlockHitResult hit, final Projectile projectile) {
        this.onHit(level, state, hit.getBlockPos(), hit.getDirection());
    }

    @Override
    protected void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        final InvertedBellBlockEntity ibbe = getControllerBE(level, pos, state);
        if (ibbe != null) {
            ibbe.destroyBell();
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    //can probably be changed to be constant instead of iterating over all positions in a 3x3x3 volume
    private static InvertedBellBlockEntity getControllerBE(final Level level, final BlockPos pos, final BlockState ownState) {
        for (final BlockPos searchPos : BlockPos.betweenClosed(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1)) {
            if (level.getBlockEntity(searchPos) instanceof final InvertedBellBlockEntity ibbe) {
                return ibbe;
            }
        }

        return null;
    }

    @Override
    protected boolean propagatesSkylightDown(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> blockEntityType) {
        if (state.getValue(PART) == CONTROLLER_PART) {
            return createTickerHelper(blockEntityType, NMLBlockEntities.INVERTED_BELL.get(), InvertedBellBlockEntity::tick);
        }
        return null;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(InvertedBellBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(HORIZONTAL_FACING).add(PART));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return blockState.getValue(PART) == CONTROLLER_PART ? new InvertedBellBlockEntity(blockPos, blockState) : null;
    }
}
