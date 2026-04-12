package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.common.world.structure.poolelements.CenteredSinglePoolElement;
import com.farcr.nomansland.common.world.structure.poolelements.ErodedSinglePoolElement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLStructureElementTypes {

    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_ELEMENTS = DeferredRegister.create(BuiltInRegistries.STRUCTURE_POOL_ELEMENT, "nomansland");


    public static final Supplier<CenteredSinglePoolElement.Type> CENTERED_ELEMENT = register("centered_single_pool_element", CenteredSinglePoolElement.Type::new);
    public static final Supplier<ErodedSinglePoolElement.Type> ERODED_ELEMENT = register("eroded_single_pool_element", ErodedSinglePoolElement.Type::new);

    public static <X extends StructurePoolElementType<?>> DeferredHolder<StructurePoolElementType<?>, X> register(String name, Supplier<X> factory) {
        return STRUCTURE_ELEMENTS.register(name, factory);
    }
}
