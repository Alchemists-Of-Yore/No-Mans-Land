package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.blockentity.VentBlockEntity;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class VentBlock extends DirectionalBlock implements EntityBlock {
    public static final int RANGE = 5;
    private static final int MAX_AFFECTED_CELLS = 600;

    protected VentBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public abstract ParticleOptions getBubbleParticle();

    public abstract void affectEntity(Level level, BlockPos ventPos, Entity entity, int distance);

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VentBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != NMLBlockEntities.VENT.get()) return null;
        BlockEntityTicker<VentBlockEntity> ticker = level.isClientSide() ? VentBlockEntity::clientTick : VentBlockEntity::serverTick;
        return (BlockEntityTicker<T>) ticker;
    }

    public static boolean isWater(LevelAccessor level, BlockPos pos) {
        return level.getFluidState(pos).is(FluidTags.WATER);
    }

    public static Map<BlockPos, Integer> computeAffectedWater(LevelAccessor level, BlockPos ventPos, Direction facing) {
        Map<BlockPos, Integer> affected = new HashMap<>();
        BlockPos front = ventPos.relative(facing);
        if (!isWater(level, front)) return affected;

        Deque<BlockPos> queue = new ArrayDeque<>();
        affected.put(front, 1);
        queue.add(front);
        while (!queue.isEmpty() && affected.size() < MAX_AFFECTED_CELLS) {
            BlockPos current = queue.poll();
            int distance = affected.get(current);
            if (distance >= RANGE) continue;
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction).immutable();
                if (affected.containsKey(neighbor) || !isWater(level, neighbor)) continue;
                affected.put(neighbor, distance + 1);
                queue.add(neighbor);
            }
        }
        return affected;
    }

    public static boolean isAffectedBy(LevelAccessor level, BlockPos pos, Class<? extends VentBlock> type) {
        if (!isWater(level, pos)) return false;

        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        Map<BlockPos, Integer> distances = new HashMap<>();
        BlockPos start = pos.immutable();
        visited.add(start);
        distances.put(start, 1);
        queue.add(start);
        while (!queue.isEmpty() && visited.size() < MAX_AFFECTED_CELLS) {
            BlockPos current = queue.poll();
            int distance = distances.get(current);
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction).immutable();
                BlockState state = level.getBlockState(neighbor);
                if (type.isInstance(state.getBlock()) && neighbor.relative(state.getValue(FACING)).equals(current)) return true;
                if (distance >= RANGE) continue;
                if (visited.contains(neighbor) || !isWater(level, neighbor)) continue;
                visited.add(neighbor);
                distances.put(neighbor, distance + 1);
                queue.add(neighbor);
            }
        }
        return false;
    }
}
