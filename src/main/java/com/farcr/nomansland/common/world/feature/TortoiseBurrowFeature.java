package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.tazer.mixed_litter.VariantUtil;
import dev.tazer.mixed_litter.registry.MLDataAttachmentTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.ArrayList;
import java.util.List;

public class TortoiseBurrowFeature extends Feature<TortoiseBurrowFeature.Configuration> {
    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public TortoiseBurrowFeature(Codec<TortoiseBurrowFeature.Configuration> codec) {
        super(codec);
    }

    /**
     * Places the given feature at the given location.
     * During world generation, features are provided with a 3x3 region of chunks, centered on the chunk being generated, that they can safely generate into.
     *
     * @param context A context object with a reference to the level and the position
     *                the feature is being placed at
     */
    @Override
    public boolean place(FeaturePlaceContext<TortoiseBurrowFeature.Configuration> context) {
        BlockPos.MutableBlockPos blockpos = context.origin().mutable();
        WorldGenLevel worldgenlevel = context.level();
        RandomSource randomsource = context.random();
        TortoiseBurrowFeature.Configuration configuration = context.config();
        BlockState blockToPlace = configuration.validBlocks().get(randomsource.nextInt(configuration.validBlocks().size())).getState(randomsource, blockpos);
        if (!canPlace(worldgenlevel, blockpos)) {
            return false;
        } else {
            boolean tortoiseSpawned = false;
            List<BlockPos> filledPos = new ArrayList<>();
            Direction direction = findEntranceDirection(worldgenlevel, blockpos);
            if (direction == null)
                return false;
            int maxOffset = randomsource.nextIntBetweenInclusive(3, 5);
            for (int offsetPos = 0; offsetPos <= maxOffset; offsetPos++) {
                BlockPos offsetBlockPos = blockpos.relative(direction, offsetPos);
                if (offsetPos != 0) {
                    offsetBlockPos = offsetBlockPos.relative(direction, 4);
                }
                if (offsetPos == maxOffset) {
                    offsetBlockPos = offsetBlockPos.relative(direction, 3).below(2);
                }
                Direction randomDirection = Direction.from2DDataValue(randomsource.nextInt(4));
                while (randomDirection == direction || randomDirection == direction.getOpposite()) {
                    randomDirection = Direction.from2DDataValue(randomsource.nextInt(4));
                }
                filledPos.add(offsetBlockPos.mutable().move(randomDirection));
            }
            if (filledPos.stream().noneMatch(blockPos -> blockPos != filledPos.getFirst() && !checkIfAllSolid(worldgenlevel, blockPos, 5, 5, 5))) {
                if (!filledPos.isEmpty() && filledPos.size() > 2) {
                    List<BlockPos> airPos = new ArrayList<>();
                    for (int listEntry = 0; listEntry < filledPos.size(); listEntry++) {
                        BlockPos listedPos = filledPos.get(listEntry);
                        BlockPos tortoiseSpawnPos = filledPos.getLast();
                        BlockState stoneState = (listedPos.getY() <= 0 || worldgenlevel.getBiome(listedPos).is(NMLBiomes.CAVE_DEPTHS)) ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
                        if (listedPos != tortoiseSpawnPos) {
                            this.placeBurrow(5.0D, 5.0D, 5.0D, listedPos, worldgenlevel, stoneState, randomsource, listedPos != filledPos.getFirst(), stoneState, direction.getOpposite(), airPos, 1, 3);
                        }
                        if (!tortoiseSpawned) {
                            Tortoise tortoise = NMLEntities.TORTOISE.get().create(worldgenlevel.getLevel());
                            if (tortoise != null) {
                                tortoiseSpawned = true;
                                tortoise.moveTo(tortoiseSpawnPos.getX(), tortoiseSpawnPos.getY(), tortoiseSpawnPos.getZ(), 0, 0);
                                tortoise.finalizeSpawn(worldgenlevel, worldgenlevel.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, null);
                                tortoise.setHomePos(tortoiseSpawnPos);
                                tortoise.setData(MLDataAttachmentTypes.SPAWN_LOCATION, GlobalPos.of(tortoise.level().dimension(), tortoise.blockPosition()));
                                VariantUtil.applySuitableVariants(tortoise);
                                worldgenlevel.addFreshEntityWithPassengers(tortoise);
                            }
                        }
                        this.placeBurrow(5.0D, 5.0D, 5.0D, tortoiseSpawnPos, worldgenlevel, blockToPlace, randomsource, true, stoneState, direction.getOpposite(), airPos, 3, 2);
                    }
                    for (BlockPos blockPos : airPos) {
                        if (!worldgenlevel.getBlockState(blockPos).is(NMLBlocks.CAVE_WEEDS.get()))
                            worldgenlevel.setBlock(blockPos, AIR, 2);
                    }
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    private boolean checkIfAllSolid(WorldGenLevel level, BlockPos origin, double radiusX, double radiusY, double radiusZ) {
        List<BlockPos> filledPos = new ArrayList<>();
        for (int x = -8; x < 8; x++) {
            for (int y = -4; y < 5; y++) {
                for (int z = -8; z < 8; z++) {
                    double dX = (x) / (radiusX / 2.0);
                    double dY = (y) / (radiusY / 2.0);
                    double dZ = (z) / (radiusZ / 2.0);
                    double distance = (dX * dX) + (dY * dY) + (dZ * dZ);
                    BlockPos.MutableBlockPos selectedPos = origin.offset(x, y, z).mutable();
                    if (distance < 1.0D) {
                        filledPos.add(selectedPos);
                    }
                }
            }
        }
        return filledPos.stream().allMatch(blockPos -> !level.isEmptyBlock(blockPos) && level.getFluidState(blockPos).isEmpty());
    }


    private Direction findEntranceDirection(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis().isHorizontal()) {
                for (int offsetPos = 0; offsetPos < 4; offsetPos++) {
                    List<BlockPos> checkedPositions = new ArrayList<>();
                    BlockPos airPos = pos.relative(direction, offsetPos);
                    checkedPositions.add(airPos);
                    if (checkedPositions.stream().allMatch(level::isEmptyBlock)) {
                        return direction.getOpposite();
                    }
                }
            }
        }
        return null;
    }

    private void placeBurrow(double radiusX, double radiusY, double radiusZ, BlockPos origin, WorldGenLevel level, BlockState blockToPlace, RandomSource randomSource, boolean shouldBarrier, BlockState barrierState, Direction direction, List<BlockPos> airPos, int entranceOffset, int entranceFowardOffset) {
        for (int x = -8; x < 8; x++) {
            for (int y = -4; y < 5; y++) {
                for (int z = -8; z < 8; z++) {
                    double dX = (x) / (radiusX / 2.0);
                    double dY = (y) / (radiusY / 2.0);
                    double dZ = (z) / (radiusZ / 2.0);
                    double distance = (dX * dX) + (dY * dY) + (dZ * dZ);
                    BlockPos.MutableBlockPos selectedPos = origin.offset(x, y, z).mutable();
                    BlockState currentState = level.getBlockState(selectedPos);
                    BlockState belowState = level.getBlockState(selectedPos.below());
                    if (distance < 1.0D) {
                        if (this.canReplaceBlock(currentState)) {
                            boolean isTopLayer = y >= 0.0D;
                            if (!isTopLayer) {
                                if (belowState.isAir())
                                    selectedPos.move(Direction.DOWN);
                                level.setBlock(selectedPos, blockToPlace, 2);
                            } else {
                                level.setBlock(selectedPos, AIR, 2);
                                for (Direction aroundDirection : Direction.values()) {
                                    if (aroundDirection != direction && aroundDirection != direction.getOpposite()) {
                                        BlockPos toAlsoDelete = origin.relative(direction, entranceFowardOffset).above(entranceOffset);
                                        BlockPos aroundOrigin = toAlsoDelete.relative(aroundDirection);
                                        airPos.add(aroundOrigin);
                                        airPos.add(aroundOrigin.above());
                                        airPos.add(toAlsoDelete);
                                        airPos.add(toAlsoDelete.above());
                                    }
                                }
                                airPos.add(selectedPos);
                                if (level.getBlockState(selectedPos.below()).isSolid() && randomSource.nextInt(5) == 0)
                                    level.setBlock(selectedPos, NMLBlocks.CAVE_WEEDS.get().defaultBlockState(), 2);
                            }
                        }
                    }
                }
            }
        }
        if (shouldBarrier) {
            for (int x = -8; x < 8; x++) {
                for (int y = -4; y < 4; y++) {
                    for (int z = -8; z < 8; z++) {
                        double dX = (double) x / (radiusX / 2.0);
                        double dY = (double) y / (radiusY / 2.0);
                        double dZ = (double) z / (radiusZ / 2.0);
                        double distance = dX * dX + dY * dY + dZ * dZ;

                        if (distance >= 1.0) {
                            boolean nearBurrow = false;
                            for (Direction surroundingDirection : Direction.values()) {
                                double neighborDX = (double) (x + surroundingDirection.getStepX()) / (radiusX / 2.0);
                                double neighborDY = (double) (y + surroundingDirection.getStepY()) / (radiusY / 2.0);
                                double neighborDZ = (double) (z + surroundingDirection.getStepZ()) / (radiusZ / 2.0);
                                if ((neighborDX * neighborDX + neighborDY * neighborDY + neighborDZ * neighborDZ) < 1.0) {
                                    nearBurrow = true;
                                    break;
                                }
                            }
                            if (nearBurrow) {
                                BlockPos currentPos = origin.offset(x, y, z);
                                boolean flag = Math.abs(x) <= 3 && Math.abs(y) <= 3 && Math.abs(z) <= 3;
                                if (flag)
                                    level.setBlock(currentPos, barrierState, 2);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean canPlace(WorldGenLevel level, BlockPos origin) {
        int openingCount = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos currentPos = origin.offset(x, y, z);
                    boolean isSolid = level.getBlockState(currentPos).isSolid() && level.getBlockState(currentPos).getFluidState().isEmpty();
                    if (((y == -4 || y == 4) && !isSolid) || !level.getBlockState(currentPos).getFluidState().isEmpty()) {
                        return false;
                    }
                    if ((x == -2 || x == 2 || z == -2 || z == 2)
                            && y == 0
                            && level.isEmptyBlock(currentPos)
                            && level.isEmptyBlock(currentPos.above())) {
                        openingCount++;
                    }
                }
            }
        }
        return openingCount >= 1 && openingCount <= 5;
    }

    private boolean canReplaceBlock(BlockState state) {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE);
    }

    public record Configuration(List<BlockStateProvider> validBlocks) implements FeatureConfiguration {
        public static final Codec<TortoiseBurrowFeature.Configuration> CODEC = RecordCodecBuilder.create(configurationInstance ->
                configurationInstance.group(BlockStateProvider.CODEC.listOf().fieldOf("blocks").orElse(List.of()).forGetter(Configuration::validBlocks)).apply(configurationInstance, Configuration::new));
    }
}
