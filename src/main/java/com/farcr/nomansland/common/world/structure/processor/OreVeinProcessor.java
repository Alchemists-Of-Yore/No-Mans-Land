package com.farcr.nomansland.common.world.structure.processor;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureProcessorTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class OreVeinProcessor extends StructureProcessor {

    public static final MapCodec<OreVeinProcessor> CODEC = MapCodec.unit(OreVeinProcessor::new);

    private static final Map<ResourceLocation, String> VEIN_MATERIAL = Map.of(
            ResourceLocation.withDefaultNamespace("tuff"), "tuff",
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "siltstone"), "siltstone",
            ResourceLocation.withDefaultNamespace("andesite"), "andesite",
            ResourceLocation.withDefaultNamespace("granite"), "granite",
            ResourceLocation.withDefaultNamespace("diorite"), "diorite"
    );

    @Override
    protected StructureProcessorType<?> getType() {
        return NMLStructureProcessorTypes.ORE_VEIN.get();
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        BlockState worldState = levelReader.getBlockState(relativeBlockInfo.pos());
        Registry<Block> blockRegistry = levelReader.registryAccess().registryOrThrow(Registries.BLOCK);

        ResourceLocation worldBlockId = blockRegistry.getKey(worldState.getBlock());
        String material = VEIN_MATERIAL.get(worldBlockId);
        if (material == null) return relativeBlockInfo;

        BlockState newState = relativeBlockInfo.state();
        ResourceLocation newBlockId = blockRegistry.getKey(newState.getBlock());

        String replacedPath = newBlockId.getPath().replace("stone", material);
        if (replacedPath.equals(newBlockId.getPath())) return relativeBlockInfo;

        Optional<Block> replacement = blockRegistry.getOptional(ResourceLocation.withDefaultNamespace(replacedPath));
        if (replacement.isEmpty()) {
            replacement = blockRegistry.getOptional(ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, replacedPath));
        }

        if (replacement.isPresent()) {
            BlockState replacedState = replacement.get().withPropertiesOf(newState);
            return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), replacedState, relativeBlockInfo.nbt());
        }

        return relativeBlockInfo;
    }
}
