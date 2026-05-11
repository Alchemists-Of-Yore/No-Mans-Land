package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.block.pots.PotBlock;
import com.farcr.nomansland.common.block.pots.PotModifier;
import com.farcr.nomansland.common.block.pots.PotSize;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.Map;

public class PotPatchFeature extends Feature<PotPatchConfiguration> {

    public PotPatchFeature(Codec<PotPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<PotPatchConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        PotPatchConfiguration config = context.config();

        if (config.variants().isEmpty()) return false;

        int totalWeight = config.variants().stream().mapToInt(PotPatchConfiguration.WeightedVariant::weight).sum();
        if (totalWeight <= 0) return false;

        ResourceLocation potionTableId = config.potionTable().orElse(null);

        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < config.tries(); i++) {
            pos.setWithOffset(origin,
                    random.nextInt(config.xzSpread() * 2 + 1) - config.xzSpread(),
                    random.nextInt(config.ySpread() * 2 + 1) - config.ySpread(),
                    random.nextInt(config.xzSpread() * 2 + 1) - config.xzSpread());

            pos.set(findGround(level, pos, config.ySpread()));
            if (pos.getY() == Integer.MIN_VALUE) continue;

            if (!level.getBlockState(pos).isAir()) continue;
            BlockState belowState = level.getBlockState(pos.below());
            if (!belowState.isFaceSturdy(level, pos.below(), Direction.UP)) continue;
            if (belowState.getBlock() instanceof PotBlock) continue;

            PotPatchConfiguration.WeightedVariant selected = selectWeighted(config, random, totalWeight);
            ResourceLocation variantId = selected.variant();

            Registry<PotVariant> registry = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY);
            PotVariant variant = registry.getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, variantId)).orElse(null);
            if (variant == null) continue;

            BlockState potState = (variant.size() == PotSize.LARGE ? NMLBlocks.LARGE_ANCIENT_POT.get() : NMLBlocks.ANCIENT_POT.get())
                    .defaultBlockState();

            if (variant.size() == PotSize.LARGE && !level.getBlockState(pos.above()).isAir()) continue;

            Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            potState = potState.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);

            if (!level.setBlock(pos, potState, 2)) continue;

            if (variant.size() == PotSize.LARGE) {
                BlockState upperState = potState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
                if (!level.setBlock(pos.above(), upperState, 2)) {
                    continue;
                }
            }

            PotBlockEntity pot = new PotBlockEntity(pos.immutable(), potState);
            pot.variant = variant;
            for (Map.Entry<PotModifier, Float> entry : variant.modifierChances().entrySet()) {
                if (random.nextFloat() < entry.getValue()) {
                    pot.addModifier(entry.getKey());
                }
            }
            config.lootTable().ifPresent(id ->
                    pot.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, id), random.nextLong())
            );
            if (potionTableId != null && random.nextFloat() < config.potionChance()) {
                pot.setPotionTable(potionTableId, random.nextLong());
            }
            ChunkAccess chunk = level.getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
            chunk.setBlockEntity(pot);

            placed++;
        }

        return placed > 0;
    }

    private static BlockPos findGround(WorldGenLevel level, BlockPos.MutableBlockPos pos, int ySpread) {
        for (int y = 0; y <= ySpread * 2; y++) {
            BlockPos check = pos.offset(0, -y, 0);
            if (level.getBlockState(check).isAir() && level.getBlockState(check.below()).isFaceSturdy(level, check.below(), Direction.UP)) {
                return check;
            }
        }
        pos.setY(Integer.MIN_VALUE);
        return pos;
    }

    private static PotPatchConfiguration.WeightedVariant selectWeighted(PotPatchConfiguration config, RandomSource random, int totalWeight) {
        int roll = random.nextInt(totalWeight);
        for (PotPatchConfiguration.WeightedVariant entry : config.variants()) {
            roll -= entry.weight();
            if (roll < 0) return entry;
        }
        return config.variants().getLast();
    }
}
