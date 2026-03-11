package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class InvertedBellBlock extends BaseEntityBlock {
    public static BooleanProperty CONTROLLER = BooleanProperty.create("controller");

    public InvertedBellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CONTROLLER, Boolean.FALSE));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        BlockPos blockpos = context.getClickedPos();
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        Level level = context.getLevel();
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
        return this.defaultBlockState();
    }

    @Override
    public void setPlacedBy(final Level level, final BlockPos pos, final BlockState state, @Nullable final LivingEntity placer, final ItemStack stack) {
        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = 0; y < 3; y++) {
                    level.setBlock(mutPos.setWithOffset(pos, x, y, z),
                            state.setValue(CONTROLLER, x == 0 && z == 0 && y == 1),
                            3
                    );
                }
            }
        }
    }

    private boolean onHit(Level level, BlockPos pos, Direction direction) {
        if (direction.getAxis() != Direction.Axis.Y) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof InvertedBellBlockEntity ibbe) {
                InvertedBellBlockEntity controller = ibbe.getController();
                if (controller != null && controller.timer <= 0) {
                    level.blockEvent(pos, controller.getBlockState().getBlock(), 1, direction.get2DDataValue());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(final BlockState state, final Level level, final BlockPos pos, final Player player, final BlockHitResult hitResult) {
        if (player.getMainHandItem().isEmpty()) {
            if (this.onHit(level, pos, hitResult.getDirection())) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        this.onHit(level, hit.getBlockPos(), hit.getDirection());
    }

    @Override
    protected void onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof InvertedBellBlockEntity ibbe) {
            ibbe.destroyBell();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getVisualShape(final BlockState state, final BlockGetter level, final BlockPos pos, final CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return true;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, NMLBlockEntities.INVERTED_BELL.get(), InvertedBellBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(InvertedBellBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(CONTROLLER));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new InvertedBellBlockEntity(blockPos, blockState);
    }
}
