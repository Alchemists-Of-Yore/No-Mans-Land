package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NoMansLand;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, NoMansLand.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> TIME_WHEN_DISABLED = DATA_COMPONENTS.registerComponentType(
            "time_when_disabled", builder -> builder
                    .persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
}
