package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.structure.processor.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLStructureProcessorTypes {
    public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSOR_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, NoMansLand.MODID);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<PrefixProcessor>>
            PREFIX = register("prefix", PrefixProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<CandleProcessor>>
            CANDLES = register("candles", CandleProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<TallBlockRemoverProcessor>>
            TALL_BLOCK_REMOVER = register("tall_block_remover", TallBlockRemoverProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BlockRemoverProcessor>>
            BLOCK_REMOVER = register("block_remover", BlockRemoverProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<InvertedBellProcessor>>
            BELL_PROCESSOR = register("inverted_bell", InvertedBellProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<FeatureProcessor>>
            FEATURE = register("feature", FeatureProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<OreVeinProcessor>>
            ORE_VEIN = register("ore_vein", OreVeinProcessor.CODEC);
    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<BlockStateModifierProcessor>>
            BLOCKSTATE_MODIFIER = register("blockstate_modifier", BlockStateModifierProcessor.CODEC);

    private static <P extends StructureProcessor> DeferredHolder<StructureProcessorType<?>, StructureProcessorType<P>> register (String name, MapCodec<P> codec) {
        return STRUCTURE_PROCESSOR_TYPES.register(name, () -> () -> codec);
    }
}
