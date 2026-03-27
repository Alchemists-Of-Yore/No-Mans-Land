package com.farcr.nomansland.common.world.structure.processor;

import com.farcr.nomansland.common.registry.worldgen.NMLStructureProcessorTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PotFeatureProcessor extends StructureProcessor {

    public static final MapCodec<PotFeatureProcessor> CODEC = MapCodec.unit(PotFeatureProcessor::new);

    private static final ResourceKey<ConfiguredFeature<?, ?>> TREASURE = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath("nomansland", "pots/pots_alchemist_treasure"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> POTIONS = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath("nomansland", "pots/pots_alchemist_potions"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> ALCHEMY = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath("nomansland", "pots/pots_alchemist_alchemy"));
    private static final ResourceKey<ConfiguredFeature<?, ?>> ARTISANSHIP = ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath("nomansland", "pots/pots_alchemist_artisanship"));

    @Override
    protected StructureProcessorType<?> getType() {
        return NMLStructureProcessorTypes.POT_FEATURE.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(ServerLevelAccessor level, BlockPos offset, BlockPos pos, List<StructureTemplate.StructureBlockInfo> originalBlockInfos, List<StructureTemplate.StructureBlockInfo> processedBlockInfos, StructurePlaceSettings settings) {
        List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>(List.copyOf(processedBlockInfos));

        if (level instanceof WorldGenLevel worldGenLevel) {
            for (StructureTemplate.StructureBlockInfo info : processedBlockInfos) {
                ResourceKey<ConfiguredFeature<?, ?>> featureKey = getFeatureForConcrete(info.state());
                if (featureKey != null) {
                    result.remove(info);
                    result.add(new StructureTemplate.StructureBlockInfo(info.pos(), Blocks.AIR.defaultBlockState(), null));

                    worldGenLevel.registryAccess()
                            .registryOrThrow(Registries.CONFIGURED_FEATURE)
                            .getHolder(featureKey)
                            .ifPresent(holder -> holder.value().place(
                                    worldGenLevel,
                                    worldGenLevel.getLevel().getChunkSource().getGenerator(),
                                    settings.getRandom(info.pos()),
                                    info.pos()
                            ));
                }
            }
        }

        return result;
    }

    @Nullable
    private static ResourceKey<ConfiguredFeature<?, ?>> getFeatureForConcrete(BlockState state) {
        if (state.is(Blocks.BLUE_CONCRETE)) return TREASURE;
        if (state.is(Blocks.RED_CONCRETE)) return POTIONS;
        if (state.is(Blocks.YELLOW_CONCRETE)) return ALCHEMY;
        if (state.is(Blocks.ORANGE_CONCRETE)) return ARTISANSHIP;
        return null;
    }
}
