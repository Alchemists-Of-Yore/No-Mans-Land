package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.VentBlock;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VentBlockEntity extends BlockEntity {
    private static final int RECOMPUTE_INTERVAL = 40;

    private final Map<BlockPos, Integer> affectedWater = new HashMap<>();
    private final List<BlockPos> affectedCells = new ArrayList<>();
    private final List<BlockPos> surfaceCells = new ArrayList<>();
    private AABB bounds;
    private int recomputeCooldown;

    public VentBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.VENT.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VentBlockEntity vent) {
        if (!(state.getBlock() instanceof VentBlock ventBlock)) return;
        vent.refreshAffectedWater(level, pos, state);
        if (vent.affectedWater.isEmpty()) return;

        for (Entity entity : level.getEntitiesOfClass(Entity.class, vent.bounds)) {
            int distance = vent.touchDistance(entity);
            if (distance < 0) continue;
            ventBlock.affectEntity(level, pos, entity, distance);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, VentBlockEntity vent) {
        if (!(state.getBlock() instanceof VentBlock ventBlock)) return;
        vent.refreshAffectedWater(level, pos, state);
        if (vent.affectedWater.isEmpty()) return;

        ParticleOptions bubble = ventBlock.getBubbleParticle();
        RandomSource random = level.getRandom();
        Direction facing = state.getValue(VentBlock.FACING);

        BlockPos front = pos.relative(facing);
        if (vent.affectedWater.containsKey(front)) {
            for (int i = 0; i < 2; i++) {
                if (random.nextFloat() >= 0.6F) continue;
                double x = pos.getX() + 0.5 + facing.getStepX() * 0.6 + (random.nextDouble() - 0.5) * 0.5;
                double y = pos.getY() + 0.5 + facing.getStepY() * 0.6 + (random.nextDouble() - 0.5) * 0.5;
                double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.6 + (random.nextDouble() - 0.5) * 0.5;
                level.addParticle(bubble, x, y, z,
                        facing.getStepX() * 0.08 + (random.nextDouble() - 0.5) * 0.05,
                        0.06 + random.nextDouble() * 0.06 + facing.getStepY() * 0.08,
                        facing.getStepZ() * 0.08 + (random.nextDouble() - 0.5) * 0.05);
            }
        }

        int bubbleCount = 1 + vent.affectedCells.size() / 12;
        for (int i = 0; i < bubbleCount; i++) {
            BlockPos cell = vent.affectedCells.get(random.nextInt(vent.affectedCells.size()));
            level.addParticle(bubble,
                    cell.getX() + random.nextDouble(), cell.getY() + random.nextDouble(), cell.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.03, 0.05 + random.nextDouble() * 0.05, (random.nextDouble() - 0.5) * 0.03);
        }

        if (!vent.surfaceCells.isEmpty()) {
            for (int i = 0; i < 1 + vent.surfaceCells.size() / 16; i++) {
                if (random.nextFloat() >= 0.3F) continue;
                BlockPos cell = vent.surfaceCells.get(random.nextInt(vent.surfaceCells.size()));
                level.addParticle(ParticleTypes.CLOUD,
                        cell.getX() + random.nextDouble(), cell.getY() + 1.0 + random.nextDouble() * 0.2, cell.getZ() + random.nextDouble(),
                        (random.nextDouble() - 0.5) * 0.02, 0.03 + random.nextDouble() * 0.03, (random.nextDouble() - 0.5) * 0.02);
            }

            if (random.nextInt(80) == 0) {
                BlockPos cell = vent.surfaceCells.get(random.nextInt(vent.surfaceCells.size()));
                level.playLocalSound(cell.getX() + 0.5, cell.getY() + 1.0, cell.getZ() + 0.5,
                        SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS,
                        0.4F, 0.8F + random.nextFloat() * 0.4F, false);
            }
        }
    }

    private void refreshAffectedWater(Level level, BlockPos pos, BlockState state) {
        if (recomputeCooldown-- > 0) return;
        recomputeCooldown = RECOMPUTE_INTERVAL;

        affectedWater.clear();
        affectedCells.clear();
        surfaceCells.clear();
        bounds = null;
        affectedWater.putAll(VentBlock.computeAffectedWater(level, pos, state.getValue(VentBlock.FACING)));
        if (affectedWater.isEmpty()) return;

        affectedCells.addAll(affectedWater.keySet());
        for (BlockPos cell : affectedCells) {
            if (!VentBlock.isWater(level, cell.above())) surfaceCells.add(cell);
        }

        AABB box = null;
        for (BlockPos cell : affectedCells) {
            AABB cellBox = new AABB(cell.getX(), cell.getY(), cell.getZ(), cell.getX() + 1, cell.getY() + 1, cell.getZ() + 1);
            box = box == null ? cellBox : box.minmax(cellBox);
        }
        bounds = box.inflate(0.5);
    }

    private int touchDistance(Entity entity) {
        int best = -1;
        AABB box = entity.getBoundingBox().deflate(0.001);
        for (BlockPos cell : BlockPos.betweenClosed(
                Mth.floor(box.minX), Mth.floor(box.minY), Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            Integer distance = affectedWater.get(cell);
            if (distance != null && (best < 0 || distance < best)) best = distance;
        }
        return best;
    }
}
