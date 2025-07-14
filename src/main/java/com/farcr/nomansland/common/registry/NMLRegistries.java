package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.ambience.fogmodifiers.FogModifier;
import com.farcr.nomansland.common.block.tap.TapInteraction;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlock;
import com.farcr.nomansland.common.world.feature.decorator.BoulderDecoratorType;
import com.farcr.nomansland.common.world.feature.decorator.FallenTreeDecoratorType;
import com.farcr.nomansland.common.world.feature.decorator.PondDecoratorType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class NMLRegistries {
    public static final ResourceKey<Registry<PondDecoratorType<?>>> POND_DECORATOR_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("worldgen/ponddecorator"));
    public static final Registry<PondDecoratorType<?>> POND_DECORATOR_TYPE = new RegistryBuilder<>(POND_DECORATOR_TYPE_KEY).create();

    public static final ResourceKey<Registry<BoulderDecoratorType<?>>> BOULDER_DECORATOR_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("worldgen/boulderdecorator"));
    public static final Registry<BoulderDecoratorType<?>> BOULDER_DECORATOR_TYPE = new RegistryBuilder<>(BOULDER_DECORATOR_TYPE_KEY).create();

    public static final ResourceKey<Registry<FallenTreeDecoratorType<?>>> FALLEN_TREE_DECORATOR_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("worldgen/fallentreedecorator"));
    public static final Registry<FallenTreeDecoratorType<?>> FALLEN_TREE_DECORATOR_TYPE = new RegistryBuilder<>(FALLEN_TREE_DECORATOR_TYPE_KEY).create();

    public static final ResourceKey<Registry<FogModifier>> FOG_MODIFIER_TYPE_KEY = ResourceKey.createRegistryKey(NoMansLand.location("client/fogmodifier"));
    public static final Registry<FogModifier> FOG_MODIFIERS = new RegistryBuilder<>(FOG_MODIFIER_TYPE_KEY).create();

    public static final ResourceKey<Registry<TapInteraction>> TAP_INTERACTION_KEY = ResourceKey.createRegistryKey(NoMansLand.location("tapping"));

    public static final ResourceKey<Registry<ExtinguishableBlock>> EXTINGUISHABLE_BLOCKS_KEY = ResourceKey.createRegistryKey(NoMansLand.location("extinguishable_blocks"));
    public static final Registry<ExtinguishableBlock> EXTINGUISHABLE_BLOCKS = new RegistryBuilder<>(EXTINGUISHABLE_BLOCKS_KEY).create();
}
