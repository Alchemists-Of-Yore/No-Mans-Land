package com.farcr.nomansland.common.world.structure.processor;

import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureProcessorTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

public class InvertedBellProcessor extends StructureProcessor {
    public static final MapCodec<InvertedBellProcessor> CODEC = MapCodec.unit(InvertedBellProcessor::new);

    @Override
    protected StructureProcessorType<?> getType() {
        return NMLStructureProcessorTypes.BELL_PROCESSOR.get();
    }

    @Override
    public @Nullable StructureTemplate.StructureBlockInfo process(LevelReader levelReader, BlockPos offset, BlockPos pos, StructureTemplate.StructureBlockInfo blockInfo, StructureTemplate.StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        if (relativeBlockInfo.state().is(NMLBlocks.INVERTED_BELL) &&
                relativeBlockInfo.state().getValue(InvertedBellBlock.PART) == InvertedBellBlock.CONTROLLER_PART) {
            CompoundTag nbt = relativeBlockInfo.nbt();
            nbt.putInt("State", 1);
            return new StructureTemplate.StructureBlockInfo(
                    relativeBlockInfo.pos(), relativeBlockInfo.state(), nbt);
        }

        return relativeBlockInfo;
    }
}
