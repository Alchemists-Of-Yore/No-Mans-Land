package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.registry.NMLParticleTypes;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ToxicGasBlock extends Block {
    public static final IntegerProperty DISPERSION = IntegerProperty.create("dispersion", 0, 6);
    public static final int MAX_DISPERSION = 6;
    public static final double HITBOX_HALF = 0.875;
    private static final int MOVE_DELAY = 5;
    private static final Direction[] HORIZONTAL = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

    public ToxicGasBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISPERSION, MAX_DISPERSION));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DISPERSION);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        if (!oldState.is(this)) level.scheduleTick(pos, this, MOVE_DELAY);
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!level.isClientSide() && !newState.is(this) && !newState.isAir() && newState.getFluidState().isEmpty() && level instanceof ServerLevel serverLevel) {
            displace(serverLevel, pos, state.getValue(DISPERSION));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    private static void displace(ServerLevel level, BlockPos pos, int dispersion) {
        List<Direction> directions = new ArrayList<>(List.of(HORIZONTAL));
        Util.shuffle(directions, level.random);
        for (Direction direction : directions) {
            BlockPos target = pos.relative(direction);
            if (canFlowInto(level.getBlockState(target))) {
                place(level, target, dispersion);
                return;
            }
        }
        BlockPos up = pos.above();
        if (canFlowInto(level.getBlockState(up))) place(level, up, dispersion);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextBoolean()) level.sendParticles(NMLParticleTypes.TOXIC_GAS.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.45, 0.45, 0.45, 0.0);

        BlockPos below = pos.below();
        if (canFlowInto(level.getBlockState(below))) {
            int dispersion = state.getValue(DISPERSION);
            level.removeBlock(pos, false);
            place(level, below, dispersion);
            return;
        }

        int dispersion = state.getValue(DISPERSION);
        float chance = 0.03F + (MAX_DISPERSION - dispersion) * 0.04F;
        if (level.getBlockState(pos.above()).isAir()) chance += 0.08F;
        if (random.nextFloat() < chance) {
            level.removeBlock(pos, false);
            return;
        }

        if (dispersion > 0) {
            for (Direction direction : HORIZONTAL) {
                BlockPos target = pos.relative(direction);
                if (canFlowInto(level.getBlockState(target))) {
                    place(level, target, dispersion - 1);
                }
            }
        }

        level.scheduleTick(pos, this, MOVE_DELAY + random.nextInt(MOVE_DELAY));
    }

    public static boolean isToxicGas(BlockState state) {
        return state.getBlock() instanceof ToxicGasBlock;
    }

    public static boolean canFlowInto(BlockState state) {
        if (isToxicGas(state)) return false;
        if (state.isAir()) return true;
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    public static void place(LevelAccessor level, BlockPos pos, int dispersion) {
        Block block = NMLBlocks.TOXIC_GAS.get();
        BlockState state = block.defaultBlockState().setValue(DISPERSION, Math.max(0, Math.min(MAX_DISPERSION, dispersion)));
        level.setBlock(pos, state, 3);
        level.scheduleTick(pos, block, MOVE_DELAY);
    }

    public static void scatterAround(ServerLevel level, BlockPos center, int count) {
        List<BlockPos> candidates = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (canFlowInto(level.getBlockState(pos))) candidates.add(pos);
                }
            }
        }
        Util.shuffle(candidates, level.random);
        int placed = 0;
        for (BlockPos pos : candidates) {
            if (placed >= count) break;
            place(level, pos, MAX_DISPERSION);
            placed++;
        }
    }

    public static void emitInFront(ServerLevel level, BlockPos containerPos, Direction facing) {
        BlockPos front = containerPos.relative(facing);
        if (canFlowInto(level.getBlockState(front))) {
            place(level, front, MAX_DISPERSION);
        }
    }

    public static void clearArea(ServerLevel level, Vec3 center, double radius) {
        int r = Mth.ceil(radius);
        double radiusSq = radius * radius;
        BlockPos origin = BlockPos.containing(center);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-r, -r, -r), origin.offset(r, r, r))) {
            if (isToxicGas(level.getBlockState(pos)) && pos.distToCenterSqr(center.x, center.y, center.z) <= radiusSq) {
                level.removeBlock(pos, false);
            }
        }
    }

    private static AABB gasBox(BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        return new AABB(cx - HITBOX_HALF, cy - HITBOX_HALF, cz - HITBOX_HALF, cx + HITBOX_HALF, cy + HITBOX_HALF, cz + HITBOX_HALF);
    }

    public static boolean isBodyInGas(LivingEntity entity) {
        Level level = entity.level();
        AABB box = entity.getBoundingBox();
        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(box.minX - 1, box.minY - 1, box.minZ - 1),
                BlockPos.containing(box.maxX + 1, box.maxY + 1, box.maxZ + 1))) {
            if (isToxicGas(level.getBlockState(pos)) && gasBox(pos).intersects(box)) return true;
        }
        return false;
    }

    public static boolean isHeadInGas(LivingEntity entity) {
        Level level = entity.level();
        Vec3 eye = entity.getEyePosition();
        BlockPos origin = BlockPos.containing(eye);
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (isToxicGas(level.getBlockState(pos)) && gasBox(pos).contains(eye)) return true;
                }
            }
        }
        return false;
    }
}
