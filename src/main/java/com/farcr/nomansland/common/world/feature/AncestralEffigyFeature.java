package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.block.AncestralEffigyBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class AncestralEffigyFeature extends Feature<AncestralEffigyFeature.Configuration> {

    public AncestralEffigyFeature() {
        super(Configuration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Configuration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        Configuration config = context.config();

        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < config.tries; i++) {
            pos.setWithOffset(origin,
                    random.nextInt(config.xzSpread * 2 + 1) - config.xzSpread,
                    random.nextInt(config.ySpread * 2 + 1) - config.ySpread,
                    random.nextInt(config.xzSpread * 2 + 1) - config.xzSpread);

            if (!level.getBlockState(pos).isAir()) continue;

            BlockState belowState = level.getBlockState(pos.below());
            boolean onEffigy = belowState.getBlock() instanceof AncestralEffigyBlock;
            if (!onEffigy && !belowState.isFaceSturdy(level, pos.below(), Direction.UP)) continue;

            placed += placeStack(level, pos, random, config.stackChance);
        }

        return placed > 0;
    }

    private int placeStack(WorldGenLevel level, BlockPos.MutableBlockPos pos, RandomSource random, float stackChance) {
        int placed = 0;
        do {
            if (!level.getBlockState(pos).isAir()) break;

            BlockState belowState = level.getBlockState(pos.below());
            boolean onEffigy = belowState.getBlock() instanceof AncestralEffigyBlock;
            if (!onEffigy && !belowState.isFaceSturdy(level, pos.below(), Direction.UP)) break;

            Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            level.setBlock(pos, NMLBlocks.ANCESTRAL_EFFIGY.get().defaultBlockState()
                    .setValue(AncestralEffigyBlock.FACING, facing), 2);

            if (onEffigy) {
                level.setBlock(pos.below(), belowState.setValue(AncestralEffigyBlock.UP, true), 2);
            }

            placed++;
            pos.move(Direction.UP);
        } while (random.nextFloat() < stackChance);

        return placed;
    }

    public record Configuration(int tries, int xzSpread, int ySpread, float stackChance) implements FeatureConfiguration {
        public static final Codec<Configuration> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.intRange(1, 256).fieldOf("tries").forGetter(Configuration::tries),
                        Codec.intRange(0, 16).fieldOf("xz_spread").forGetter(Configuration::xzSpread),
                        Codec.intRange(0, 16).fieldOf("y_spread").forGetter(Configuration::ySpread),
                        Codec.floatRange(0.0F, 1.0F).fieldOf("stack_chance").forGetter(Configuration::stackChance)
                ).apply(instance, Configuration::new));
    }
}
