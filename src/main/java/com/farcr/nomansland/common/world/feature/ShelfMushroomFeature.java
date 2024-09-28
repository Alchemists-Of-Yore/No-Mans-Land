package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.block.ShelfMushroomBlock;
import com.farcr.nomansland.common.registry.NMLBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ShelfMushroomFeature extends Feature<NoneFeatureConfiguration> {
    public ShelfMushroomFeature(Codec<NoneFeatureConfiguration> p_67337_) {
        super(p_67337_);
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockPos blockpos = context.origin();
        context.config();
        if (worldgenlevel.isEmptyBlock(blockpos)) {
            for (Direction direction : Direction.values()) {
                if (direction != Direction.DOWN && direction != Direction.UP && ShelfMushroomBlock.isAcceptableNeighbour(worldgenlevel, blockpos.relative(direction)) && !worldgenlevel.getBlockState(blockpos).is(NMLBlocks.SHELF_MUSHROOM)) {
                    worldgenlevel.setBlock(blockpos, NMLBlocks.SHELF_MUSHROOM.get().defaultBlockState().setValue(ShelfMushroomBlock.FACING, direction), 2);
                    return true;
                }
            }
        }
        return false;
    }
}
