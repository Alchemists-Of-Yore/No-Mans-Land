package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.world.feature.decorator.BoulderDecorator;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

public class BoulderFeature extends Feature<BoulderFeatureConfiguration> {
    public BoulderFeature(Codec<BoulderFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BoulderFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        BoulderFeatureConfiguration config = context.config();

        int numCubes = config.numCubes().sample(random);
        float cubeHeightDecrease = config.cubeHeightDecrease();

        Set<BlockPos> stonePos = new HashSet<>();
        ArrayList<BlockPos> decoPos = new ArrayList<>();

        ArrayList<BlockPos> cubeFloorPlan = new ArrayList<>();
        cubeFloorPlan.add(origin);

        BlockPos.MutableBlockPos posMutable = origin.mutable();

        for (int i = 0; i < numCubes; i++) {
            BlockPos cubeOrigin = cubeFloorPlan.get(random.nextInt(cubeFloorPlan.size()));
            //BlockPos cubeOrigin = new BlockPos(cubeOriginRaw.getX() + random.nextInt(2) - 1,  cubeOriginRaw.getY(), cubeOriginRaw.getZ() + random.nextInt(2) - 1);

            int cubeHeight = Math.max(config.cubeHeight().sample(random) - (int)(cubeHeightDecrease * i), 0);
            int numErodedBlocks = config.numErodedBlocks().sample(random);
            ArrayList<BlockPos> topBlocks = new ArrayList<>();

            cubeFloorPlan.remove(cubeOrigin);
            cubeFloorPlan.add(cubeOrigin.north().east());
            cubeFloorPlan.add(cubeOrigin.north().west());
            cubeFloorPlan.add(cubeOrigin.south().east());
            cubeFloorPlan.add(cubeOrigin.south().west());
            for (int x = cubeOrigin.getX(); x <= cubeOrigin.getX() + 1; x++) {
                for (int z = cubeOrigin.getZ(); z <= cubeOrigin.getZ() + 1; z++) {
                    for (int y = cubeOrigin.getY(); y <= cubeOrigin.getY() + cubeHeight; y++) {
                        posMutable.set(x, y, z);
                        //level.setBlock(posMutable, config.blockProvider().getState(random, posMutable), 2);
                        stonePos.add(posMutable.immutable());
                        if (y == cubeOrigin.getY() + cubeHeight )
                            topBlocks.add(posMutable.immutable());
                    }
                    for (int j = 0; j < numErodedBlocks && !topBlocks.isEmpty(); j++) {
                        int blockIdx = random.nextInt(topBlocks.size());
                        posMutable.set(topBlocks.get(blockIdx));
                        if (level.getBlockState(posMutable.above()).isAir() && !stonePos.contains(posMutable.above().immutable())) {
                            stonePos.remove(posMutable.immutable());
                            topBlocks.remove(blockIdx);
                        }
                    }
                }
            }
        }

        ArrayList<BlockPos> stonePosList = new ArrayList<>();
        for (BlockPos pos : stonePos) {
            if (level.getBlockState(pos).is(BlockTags.REPLACEABLE)) {
                stonePosList.add(pos);
            }
        }
        stonePosList.sort(Comparator.comparingInt(Vec3i::getY));

        Set<BlockPos> gravityStonePos = new HashSet<>();
        for (BlockPos pos : stonePosList) {
            int i = 0;
            while (i < 10 && level.getBlockState(pos.below(i)).is(BlockTags.REPLACEABLE) && !gravityStonePos.contains(pos.below(i))) {
                i++;
            }
            if (level.getBlockState(pos.below(i)).is(Blocks.POWDER_SNOW)) return false;
            if (i == 10) return false;
            i--;
            gravityStonePos.add(pos.below(i));
        }

        for (BlockPos pos : gravityStonePos) {
            level.setBlock(pos, config.blockProvider().getState(random, pos), 2);
        }

        BiConsumer<BlockPos, BlockState> decoratorConsumer = (pos1, state1) -> {
            decoPos.add(pos1.immutable());
            level.setBlock(pos1, state1, 19);
        };
        if (!config.decorators().isEmpty()) {
            BoulderDecorator.Context boulderdecorator$context = new BoulderDecorator.Context(level, decoratorConsumer, random, gravityStonePos, context.chunkGenerator());
            config.decorators().forEach((deco) -> deco.place(boulderdecorator$context));
        }

        return true;
    }
}
