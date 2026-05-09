package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.BitSet;
import java.util.function.Function;

public class SuspiciousOreFeature extends Feature<SuspiciousOreFeatureConfiguration> {
    public SuspiciousOreFeature(Codec<SuspiciousOreFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<SuspiciousOreFeatureConfiguration> context) {
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        WorldGenLevel level = context.level();
        SuspiciousOreFeatureConfiguration config = context.config();

        float angle = random.nextFloat() * (float) Math.PI;
        float halfSize = (float) config.size() / 8.0F;
        int range = Mth.ceil(((float) config.size() / 16.0F * 2.0F + 1.0F) / 2.0F);
        double x1 = (double) origin.getX() + Math.sin(angle) * halfSize;
        double x2 = (double) origin.getX() - Math.sin(angle) * halfSize;
        double z1 = (double) origin.getZ() + Math.cos(angle) * halfSize;
        double z2 = (double) origin.getZ() - Math.cos(angle) * halfSize;
        double y1 = origin.getY() + random.nextInt(3) - 2;
        double y2 = origin.getY() + random.nextInt(3) - 2;
        int xMin = origin.getX() - Mth.ceil(halfSize) - range;
        int yMin = origin.getY() - 2 - range;
        int zMin = origin.getZ() - Mth.ceil(halfSize) - range;
        int xRange = 2 * (Mth.ceil(halfSize) + range);
        int yRange = 2 * (2 + range);

        return doPlace(level, random, config, x1, x2, z1, z2, y1, y2, xMin, yMin, zMin, xRange, yRange);
    }

    private boolean doPlace(WorldGenLevel level, RandomSource random, SuspiciousOreFeatureConfiguration config, double x1, double x2, double z1, double z2, double y1, double y2, int xMin, int yMin, int zMin, int xRange, int yRange) {
        int placed = 0;
        BitSet mask = new BitSet(xRange * yRange * xRange);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int size = config.size();
        double[] data = new double[size * 4];

        for (int n = 0; n < size; n++) {
            float t = (float) n / (float) size;
            double xn = Mth.lerp(t, x1, x2);
            double yn = Mth.lerp(t, y1, y2);
            double zn = Mth.lerp(t, z1, z2);
            double radius = random.nextDouble() * (double) size / 16.0;
            double rxz = ((Mth.sin((float) Math.PI * t) + 1.0F) * radius + 1.0) / 2.0;
            data[n * 4] = xn;
            data[n * 4 + 1] = yn;
            data[n * 4 + 2] = zn;
            data[n * 4 + 3] = rxz;
        }

        for (int p = 0; p < size - 1; p++) {
            if (data[p * 4 + 3] <= 0) continue;
            for (int q = p + 1; q < size; q++) {
                if (data[q * 4 + 3] <= 0) continue;
                double dx = data[p * 4] - data[q * 4];
                double dy = data[p * 4 + 1] - data[q * 4 + 1];
                double dz = data[p * 4 + 2] - data[q * 4 + 2];
                double dr = data[p * 4 + 3] - data[q * 4 + 3];
                if (dr * dr > dx * dx + dy * dy + dz * dz) {
                    if (dr > 0) data[q * 4 + 3] = -1;
                    else data[p * 4 + 3] = -1;
                }
            }
        }

        ResourceKey<LootTable> lootKey = config.lootTable()
                .map(id -> ResourceKey.create(Registries.LOOT_TABLE, id))
                .orElse(null);

        for (int n = 0; n < size; n++) {
            double rxz = data[n * 4 + 3];
            if (rxz < 0) continue;
            double xn = data[n * 4];
            double yn = data[n * 4 + 1];
            double zn = data[n * 4 + 2];
            int xLow = Math.max(Mth.floor(xn - rxz), xMin);
            int yLow = Math.max(Mth.floor(yn - rxz), yMin);
            int zLow = Math.max(Mth.floor(zn - rxz), zMin);
            int xHigh = Math.max(Mth.floor(xn + rxz), xLow);
            int yHigh = Math.max(Mth.floor(yn + rxz), yLow);
            int zHigh = Math.max(Mth.floor(zn + rxz), zLow);

            for (int x = xLow; x <= xHigh; x++) {
                double dxsq = ((double) x + 0.5 - xn) / rxz;
                dxsq *= dxsq;
                if (dxsq >= 1.0) continue;
                for (int y = yLow; y <= yHigh; y++) {
                    double dysq = ((double) y + 0.5 - yn) / rxz;
                    dysq *= dysq;
                    if (dxsq + dysq >= 1.0) continue;
                    for (int z = zLow; z <= zHigh; z++) {
                        double dzsq = ((double) z + 0.5 - zn) / rxz;
                        dzsq *= dzsq;
                        if (dxsq + dysq + dzsq >= 1.0) continue;

                        int idx = x - xMin + (y - yMin) * xRange + (z - zMin) * xRange * yRange;
                        if (mask.get(idx)) continue;
                        mask.set(idx);
                        mutable.set(x, y, z);
                        if (!level.ensureCanWrite(mutable)) continue;

                        BlockState existing = level.getBlockState(mutable);
                        for (OreConfiguration.TargetBlockState target : config.targetStates()) {
                            if (canPlaceOre(existing, level::getBlockState, random, config, target, mutable)) {
                                boolean suspicious = random.nextFloat() < config.suspiciousChance();
                                BlockState toPlace = suspicious ? config.suspiciousState() : target.state;
                                level.setBlock(mutable, toPlace, 2);
                                if (suspicious && lootKey != null && level.getBlockEntity(mutable) instanceof BrushableBlockEntity brushable) {
                                    brushable.setLootTable(lootKey, random.nextLong());
                                }
                                placed++;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return placed > 0;
    }

    private static boolean canPlaceOre(BlockState state, Function<BlockPos, BlockState> stateGetter, RandomSource random, SuspiciousOreFeatureConfiguration config, OreConfiguration.TargetBlockState target, BlockPos.MutableBlockPos pos) {
        if (!target.target.test(state, random)) return false;
        return shouldSkipAirCheck(random, config.discardChanceOnAirExposure()) || !isAdjacentToAir(stateGetter, pos);
    }

    private static boolean shouldSkipAirCheck(RandomSource random, float chance) {
        if (chance <= 0.0F) return true;
        return chance < 1.0F && random.nextFloat() >= chance;
    }
}
