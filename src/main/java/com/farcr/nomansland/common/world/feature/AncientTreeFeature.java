package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.BeardMossBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;

public class AncientTreeFeature extends Feature<NoneFeatureConfiguration> {
    public AncientTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource randomSource = context.random();
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos.MutableBlockPos placementPos = origin.mutable();

        // trunk shape
        int radius = (int) Math.round(randomSource.triangle(5, 2.5));
        BlockPos.MutableBlockPos[] initialTrunkPositions = new BlockPos.MutableBlockPos[12];
        BlockPos.MutableBlockPos[] trunkPositions = new BlockPos.MutableBlockPos[12];
        for (int i = 0; i < trunkPositions.length; i++) {
            float angle = Mth.TWO_PI * ((float) i / trunkPositions.length);
            BlockPos trunkPos = origin.offset(
                    Math.round(Mth.sin(angle) * radius), -1,
                    Math.round(Mth.cos(angle) * radius)
            );
            initialTrunkPositions[i] = trunkPos.mutable();
            trunkPositions[i] = trunkPos.mutable();
        }

        List<BranchOrigin> branches = new ArrayList<>();
        // place trunk
        int height = (int) randomSource.triangle(64, 24);
        for (int i = 0; i < height; i++) {
            // move up
            for (int j = 0; j < trunkPositions.length; j++) {
                BlockPos.MutableBlockPos trunkPos = trunkPositions[j];
                trunkPos.move(Direction.UP);
            }
            if (randomSource.nextInt(24) == 0 || i >= height - 2) {
                // decrease radius
                for (int j = 0; j < trunkPositions.length; j++) {
                    if (randomSource.nextInt(5) == 0) continue;
                    BlockPos.MutableBlockPos trunkPos = trunkPositions[j];
                    BlockPos.MutableBlockPos initialTrunkPos = initialTrunkPositions[j];

                    double nX = origin.getX() - trunkPos.getX(),
                           nZ = origin.getZ() - trunkPos.getZ();
                    double length = Mth.length(nX, nZ);
                    nX /= length; nZ /= length;
                    trunkPos.move((int) Math.round(nX), 0, (int) Math.round(nZ));

                    nX = origin.getX() - initialTrunkPos.getX();
                    nZ = origin.getZ() - initialTrunkPos.getZ();
                    length = Mth.length(nX, nZ);
                    nX /= length; nZ /= length;
                    initialTrunkPos.move((int) Math.round(nX), 0, (int) Math.round(nZ));
                }
            } else {
                // wobble
                for (int j = 0; j < trunkPositions.length; j++) {
                    if (randomSource.nextInt(24) != 0) continue;
                    BlockPos.MutableBlockPos trunkPos = trunkPositions[j];

                    Direction offsetDirection = Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
                    trunkPos.move(offsetDirection);

                    BlockPos initialTrunkPos = initialTrunkPositions[j];
                    if (Mth.lengthSquared(initialTrunkPos.getX() - trunkPos.getX(), initialTrunkPos.getZ() - trunkPos.getZ()) >= 2 * 2) {
                        trunkPos.move(offsetDirection.getOpposite(), 2);
                    }
                }
            }

            // relax
            double averageDistance = 0;
            for (int j = 0; j < trunkPositions.length; j++) {
                BlockPos.MutableBlockPos initialTrunkPos = initialTrunkPositions[j];
                averageDistance += Mth.length(initialTrunkPos.getX() - origin.getX(), initialTrunkPos.getZ() - origin.getZ());
            }
            averageDistance /= trunkPositions.length;
            double relaxationAmount = 0.5;
            for (int j = 0; j < trunkPositions.length; j++) {
                BlockPos.MutableBlockPos trunkPos = trunkPositions[j];
                double nX = trunkPos.getX() - origin.getX(),
                       nZ = trunkPos.getZ() - origin.getZ();
                double length = Mth.length(nX, nZ);
                nX /= length; nZ /= length;
                if (length == 0) { nX = 0; nZ = 0; }
                trunkPos.set(
                        (int) Math.round(Mth.lerp(relaxationAmount, trunkPos.getX() + 0.5, origin.getX() + nX * averageDistance)),
                        trunkPos.getY(),
                        (int) Math.round(Mth.lerp(relaxationAmount, trunkPos.getZ() + 0.5, origin.getZ() + nZ * averageDistance))
                );

                BlockPos.MutableBlockPos initialTrunkPos = initialTrunkPositions[j];
                nX = initialTrunkPos.getX() - origin.getX();
                nZ = initialTrunkPos.getZ() - origin.getZ();
                length = Mth.length(nX, nZ);
                nX /= length; nZ /= length;
                if (length == 0) { nX = 0; nZ = 0; }
                initialTrunkPos.set(
                        (int) Math.round(Mth.lerp(relaxationAmount, initialTrunkPos.getX() + 0.5, origin.getX() + nX * averageDistance)),
                        initialTrunkPos.getY(),
                        (int) Math.round(Mth.lerp(relaxationAmount, initialTrunkPos.getZ() + 0.5, origin.getZ() + nZ * averageDistance))
                );
            }


            int layerHeight = i;
            for (int j = 0; j < trunkPositions.length; j++) {
                BlockPos.MutableBlockPos pos0 = trunkPositions[j + 0];
                BlockPos.MutableBlockPos pos1 = (j + 1 < trunkPositions.length) ? trunkPositions[j + 1] : trunkPositions[0];

                line(pos0.getX(), pos0.getY(), pos0.getZ(),
                     pos1.getX(), pos1.getY(), pos1.getZ(),
                     (x, y, z) -> {
                         placementPos.set(x, y, z);
                         level.setBlock(placementPos, NMLBlocks.WALNUT.wood().block().defaultBlockState(), 2);

                         // place roots along the inner side
                         double nX = x - origin.getX(), nZ = z - origin.getZ();
                         double length = Mth.length(nX, nZ);
                         nX /= length; nZ /= length;

                         if (Math.abs(nX) > Math.abs(nZ)) {
                             placementPos.move(nX < 0 ? Direction.EAST : Direction.WEST);
                         } else {
                             placementPos.move(nZ < 0 ? Direction.SOUTH : Direction.NORTH);
                         }

                         if (level.getBlockState(placementPos).isAir())
                            level.setBlock(placementPos, NMLBlocks.WALNUT.planks().block().defaultBlockState(), 2);


                         float probabilityOfBranch = 1.0F / 200.0F;
                         if (layerHeight > height - 5) {
                             probabilityOfBranch = 1.0F / 10.0F;
                         }
                         if (randomSource.nextFloat() < probabilityOfBranch) {
                             branches.add(new BranchOrigin(placementPos.immutable(), nX, 0, nZ));
                         }
                     });
            }
        }

        for (BranchOrigin branch : branches) {
            placeBranch(level, randomSource, branch, true);
        }

        return true;
    }

    void placeRoot(WorldGenLevel level, RandomSource random, BranchOrigin branchOrigin) {

    }

    void placeBranch(WorldGenLevel level, RandomSource random, BranchOrigin branchOrigin, boolean leafy) {
        BlockPos.MutableBlockPos placementPos = branchOrigin.origin.mutable();
        double dirX = branchOrigin.directionX, dirZ = branchOrigin.directionZ;
        int branchLength = random.nextIntBetweenInclusive(5, 16);

        Direction currentDirection = random.nextBoolean() ?
                dirX > 0 ? Direction.EAST : Direction.WEST :
                dirZ > 0 ? Direction.SOUTH : Direction.NORTH;
        for (int i = 0; i < branchLength; i++) {
            float changeDirectionProbability = 0.5F;
            if (random.nextFloat() < changeDirectionProbability) {
                if (random.nextFloat() < 0.3 || i > branchLength - 3) {
                    currentDirection = Direction.UP;
                } else {
                    boolean shouldBecomeXAxis = currentDirection.getAxis() == Direction.Axis.Z;
                    if (currentDirection.getAxis() == Direction.Axis.Y) shouldBecomeXAxis = random.nextBoolean();
                    currentDirection = shouldBecomeXAxis ?
                            dirX > 0 ? Direction.EAST : Direction.WEST :
                            dirZ > 0 ? Direction.SOUTH : Direction.NORTH;
                }
            }
            placementPos.move(currentDirection);
            level.setBlock(placementPos, NMLBlocks.WALNUT.wood().block().defaultBlockState().setValue(RotatedPillarBlock.AXIS, currentDirection.getAxis()), 2);
        }

        // leaves
        if (!leafy) return;
        double radius = (int) Math.round(random.triangle(3.5, 1.5));
        BlockPos.MutableBlockPos vinePlacer = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 3; i++) {
            for (BlockPos pos :
                    BlockPos.betweenClosed((int) (placementPos.getX() - radius - 1), placementPos.getY() + i, (int) (placementPos.getZ() - radius - 1),
                            (int) (placementPos.getX() + radius + 1), placementPos.getY() + i, (int) (placementPos.getZ() + radius + 1))) {
                double distance = pos.distToCenterSqr(placementPos.getX() + 0.5, placementPos.getY() + 0.5, placementPos.getZ() + 0.5);
                if (distance < radius * radius && level.getBlockState(pos).isAir()) {
                    level.setBlock(pos, NMLBlocks.WILLOW_LEAVES.get().defaultBlockState(), 2);

                    if (i != 0 || random.nextInt(5) != 0) continue;
                    vinePlacer.set(pos);
                    vinePlacer.move(Direction.DOWN);
                    int vineLength = random.nextIntBetweenInclusive(2, 5);
                    for (int j = 0; j < vineLength; j++) {
                        vinePlacer.move(Direction.DOWN);
                        boolean tip = j == vineLength - 1 || !level.getBlockState(vinePlacer).isAir();
                        vinePlacer.move(Direction.UP);

                        BlockState state = NMLBlocks.BEARD_MOSS.get().defaultBlockState().setValue(BeardMossBlock.HALF, tip ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
                        level.setBlock(vinePlacer, state, 2);
                        vinePlacer.move(Direction.DOWN);

                        if (tip) break;
                    }
                }
            }
            radius -= random.nextFloat();
        }
    }

    static void line(int x0, int y0, int z0, int x1, int y1, int z1, TriConsumer<Integer, Integer, Integer> positionConsumer) {
        int dx = x1 - x0,
            dy = y1 - y0,
            dz = z1 - z0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        steps = Math.max(steps, Math.abs(dz));

        double xInc = (double) dx / steps,
               yInc = (double) dy / steps,
               zInc = (double) dz / steps;

        double x = x0, y = y0, z = z0;
        positionConsumer.accept((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
        for (int i = 0; i < steps; i++) {
            x += xInc;
            y += yInc;
            z += zInc;
            positionConsumer.accept((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
        }
    }

    private record BranchOrigin(BlockPos origin, double directionX, double directionY, double directionZ) {}
}
