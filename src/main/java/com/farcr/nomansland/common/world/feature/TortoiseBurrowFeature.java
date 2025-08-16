package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
        boolean tortoiseSpawned = false;
        TortoiseBurrowFeature.Configuration configuration = context.config();
        BlockState blockToPlace = configuration.validBlocks().get(randomsource.nextInt(configuration.validBlocks().size())).getState(randomsource, blockpos);
        if (!canPlace(worldgenlevel, blockpos)) {
            return false;
        } else {
            final double radiusX = 5.0D;
            final double radiusY = 5.0D;
            final double radiusZ = 5.0D;
            for (int x = -8; x < 8; x++) {
                for (int y = -4; y < 4; y++) {
                    for (int z = -8; z < 8; z++) {
                        double dX = (x) / (radiusX / 2.0);
                        double dY = (y) / (radiusY / 2.0);
                        double dZ = (z) / (radiusZ / 2.0);
                        double distance = (dX * dX) + (dY * dY) + (dZ * dZ);
                        BlockPos.MutableBlockPos selectedPos = blockpos.offset(x, y, z).mutable();
                        BlockState currentState = worldgenlevel.getBlockState(selectedPos);
                        if (distance < 1.0D) {
                            if (this.canReplaceBlock(currentState)) {
                                boolean isTopLayer = y >= 0.0D;
                                if (!isTopLayer) {
                                    worldgenlevel.setBlock(selectedPos, blockToPlace, 2);
                                } else {
                                    worldgenlevel.setBlock(selectedPos, AIR, 2);
                                    if (worldgenlevel.getBlockState(selectedPos.below()) == blockToPlace && randomsource.nextInt(5) == 0)
                                        worldgenlevel.setBlock(selectedPos, NMLBlocks.CAVE_WEEDS.get().defaultBlockState(), 2);
                                    if (!tortoiseSpawned) {
                                        Tortoise tortoise = NMLEntities.TORTOISE.get().create(worldgenlevel.getLevel());
                                        tortoiseSpawned = true;
                                        tortoise.moveTo(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 0, 0);
                                        tortoise.finalizeSpawn(worldgenlevel, worldgenlevel.getCurrentDifficultyAt(blockpos), MobSpawnType.STRUCTURE, null);
                                        tortoise.setHomePos(blockpos);
                                        worldgenlevel.getLevel().addFreshEntityWithPassengers(tortoise);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(blockpos.toString()), false);
            return true;
        }
    }

    private boolean canPlace(WorldGenLevel level, BlockPos origin) {
        int openingCount = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y < 4; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos currentPos = origin.offset(x, y, z);
                    boolean isSolid = level.getBlockState(currentPos).isSolid() && level.getBlockState(currentPos).getFluidState().isEmpty();
                    if (((y == -1 || y == 4) && !isSolid) || !level.getBlockState(currentPos).getFluidState().isEmpty()) {
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
