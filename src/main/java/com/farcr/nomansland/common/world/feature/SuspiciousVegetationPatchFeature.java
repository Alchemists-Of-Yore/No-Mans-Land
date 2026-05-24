package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.VegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Predicate;

public class SuspiciousVegetationPatchFeature extends VegetationPatchFeature {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SuspiciousVegetationPatchFeature(Codec<SuspiciousVegetationPatchConfiguration> codec) {
        super((Codec) codec);
    }

    @Override
    protected boolean placeGround(
            WorldGenLevel level,
            VegetationPatchConfiguration config,
            Predicate<BlockState> replaceableblocks,
            RandomSource random,
            BlockPos.MutableBlockPos mutablePos,
            int maxDistance
    ) {
        SuspiciousVegetationPatchConfiguration cfg = (SuspiciousVegetationPatchConfiguration) config;
        ResourceKey<LootTable> lootKey = cfg.lootTable
                .map(id -> ResourceKey.create(Registries.LOOT_TABLE, id))
                .orElse(null);

        for (int i = 0; i < maxDistance; i++) {
            BlockState ground = config.groundState.getState(random, mutablePos);
            boolean suspicious = random.nextFloat() < cfg.suspiciousChance;
            BlockState toPlace = suspicious ? cfg.suspiciousState : ground;
            BlockState existing = level.getBlockState(mutablePos);
            if (!toPlace.is(existing.getBlock())) {
                if (!replaceableblocks.test(existing)) {
                    return i != 0;
                }
                if (level.setBlock(mutablePos, toPlace, 3) && suspicious && lootKey != null) {
                    level.getBlockEntity(mutablePos, BlockEntityType.BRUSHABLE_BLOCK)
                            .ifPresent(be -> be.setLootTable(lootKey, random.nextLong()));
                }
                mutablePos.move(config.surface.getDirection());
            }
        }
        return true;
    }
}
