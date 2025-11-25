package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.world.feature.decorator.FallenTreeDecorator;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.BiConsumer;

public class FallenTreeFeature extends Feature<FallenTreeFeatureConfiguration> {
    public FallenTreeFeature(Codec<FallenTreeFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<FallenTreeFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        FallenTreeFeatureConfiguration config = context.config();

        // Scan for nearby sources blocks
        Map<Block, Integer> logScan = new HashMap<>();
        BlockPos.MutableBlockPos posMutable = origin.mutable();
        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    posMutable.set(origin.offset(x, y, z));
                    BlockState blockState = level.getBlockState(posMutable);
                    if (blockState.is(BlockTags.LOGS) && blockState.hasProperty(RotatedPillarBlock.AXIS) && blockState.isCollisionShapeFullBlock(level, posMutable)) {
                        if (logScan.containsKey(blockState.getBlock())) {
                            logScan.put(blockState.getBlock(), logScan.get(blockState.getBlock()) + 1);
                        } else logScan.put(blockState.getBlock(), 1);
                    }
                }
            }
        }

        if (logScan.isEmpty()) return false;

        // Figure out what sources block to use
        int totalBlockCount = logScan.values().stream().mapToInt(i -> i).sum();
        Block blockChoice = null;
        for (Map.Entry<Block, Integer> entry : logScan.entrySet()) {
            if (random.nextInt(totalBlockCount) < entry.getValue()) {
                blockChoice = entry.getKey();
                break;
            }
            totalBlockCount -= entry.getValue();
        }

        if (blockChoice == null)
            return false;

        int gap = config.gap().sample(random);
        int size = config.size().sample(random);
        Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);

        HashSet<BlockPos> logPos = new HashSet<>();
        ArrayList<BlockPos> decoPos = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            BlockPos pos = origin.relative(dir, gap + i + 1);
            if (!level.getBlockState(pos).is(BlockTags.REPLACEABLE) || !level.getBlockState(pos.below()).isSolid()) return false;
            logPos.add(pos);
            }

        if (!level.getBlockState(origin).is(BlockTags.REPLACEABLE) || !level.getBlockState(origin.below()).is(BlockTags.DIRT)) {
            return false;
        }

        for (BlockPos pos : logPos) {
            level.setBlock(pos, blockChoice.defaultBlockState().setValue(RotatedPillarBlock.AXIS, dir.getAxis()), 2);
        }

        if (random.nextFloat() < config.stumpProbability()) {
            level.setBlock(origin, blockChoice.defaultBlockState(), 2);
            logPos.add(origin);
        }

        BiConsumer<BlockPos, BlockState> decoratorConsumer = (pos1, state1) -> {
            decoPos.add(pos1.immutable());
            level.setBlock(pos1, state1, 19);
        };

        if (!config.decorators().isEmpty()) {
            FallenTreeDecorator.Context fallentreedecorator$context = new FallenTreeDecorator.Context(level, decoratorConsumer, random, logPos, context.chunkGenerator());
            config.decorators().forEach((deco) -> deco.place(fallentreedecorator$context));
        }

        return true;
    }
}
