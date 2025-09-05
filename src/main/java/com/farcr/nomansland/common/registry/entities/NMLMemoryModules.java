package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class NMLMemoryModules {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, NoMansLand.MODID);

    public static final Supplier<MemoryModuleType<Integer>> ANTLER_SHED_TICKS = register("antler_shed_ticks", Codec.INT);

    private static <U> Supplier<MemoryModuleType<U>> register(String key, Codec<U> codec) {
        return register(key, Optional.of(codec));
    }

    private static <U> Supplier<MemoryModuleType<U>> register(String key, Optional<Codec<U>> codec) {
        return MEMORY_MODULES.register(key, () -> new MemoryModuleType<>(codec));
    }
}
