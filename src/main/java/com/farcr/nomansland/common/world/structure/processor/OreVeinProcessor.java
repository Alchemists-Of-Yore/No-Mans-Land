package com.farcr.nomansland.common.world.structure.processor;

import com.farcr.nomansland.common.registry.worldgen.NMLStructureProcessorTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OreVeinProcessor extends StructureProcessor {

    public static final MapCodec<OreVeinProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.listOf().fieldOf("blocks").forGetter(p -> List.copyOf(p.blocks))
            ).apply(instance, OreVeinProcessor::new));

    private final Set<ResourceLocation> blocks;

    public OreVeinProcessor(List<ResourceLocation> blocks) {
        this.blocks = Set.copyOf(blocks);
    }

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
        if (!blocks.contains(worldBlockId)) return relativeBlockInfo;

        String material = worldBlockId.getPath();
        String namespace = worldBlockId.getNamespace();

        BlockState newState = relativeBlockInfo.state();
        ResourceLocation newBlockId = blockRegistry.getKey(newState.getBlock());

        String replacedPath = newBlockId.getPath().replace("stone", material);
        if (replacedPath.equals(newBlockId.getPath())) return relativeBlockInfo;

        Optional<Block> replacement = blockRegistry.getOptional(ResourceLocation.fromNamespaceAndPath(namespace, replacedPath));
        if (replacement.isEmpty() && !namespace.equals("minecraft")) {
            replacement = blockRegistry.getOptional(ResourceLocation.withDefaultNamespace(replacedPath));
        }

        if (replacement.isPresent()) {
            BlockState replacedState = replacement.get().withPropertiesOf(newState);
            return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), replacedState, relativeBlockInfo.nbt());
        }

        return relativeBlockInfo;
    }
}
