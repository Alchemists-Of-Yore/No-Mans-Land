package com.farcr.nomansland.common.world.structure.processor;

import com.farcr.nomansland.common.registry.worldgen.NMLStructureProcessorTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BlockStateModifierProcessor extends StructureProcessor {

    public static final MapCodec<BlockStateModifierProcessor> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(p -> p.block),
                    Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("properties").forGetter(p -> p.properties),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(p -> p.chance)
            ).apply(instance, BlockStateModifierProcessor::new));

    private final Block block;
    private final Map<String, String> properties;
    private final float chance;

    public BlockStateModifierProcessor(Block block, Map<String, String> properties, float chance) {
        this.block = block;
        this.properties = properties;
        this.chance = chance;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return NMLStructureProcessorTypes.BLOCKSTATE_MODIFIER.get();
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        BlockState state = relativeBlockInfo.state();
        if (!state.is(block)) return relativeBlockInfo;
        if (settings.getRandom(relativeBlockInfo.pos()).nextFloat() >= chance) return relativeBlockInfo;

        BlockState newState = state;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            Property<?> property = block.getStateDefinition().getProperty(entry.getKey());
            if (property == null) continue;
            newState = applyProperty(newState, property, entry.getValue());
        }

        return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), newState, relativeBlockInfo.nbt());
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, Property<T> property, String stringValue) {
        return property.getValue(stringValue).map(value -> state.setValue(property, value)).orElse(state);
    }
}
