package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NMLNoises {
    public static final ResourceKey<NormalNoise.NoiseParameters> CAVE_RIVER_WIGGLE_X = key("cave_river_wiggle_x");
    public static final ResourceKey<NormalNoise.NoiseParameters> CAVE_RIVER_WIGGLE_Z = key("cave_river_wiggle_z");

    public static ResourceKey<NormalNoise.NoiseParameters> key(String name) {
        return ResourceKey.create(Registries.NOISE, NoMansLand.location(name));
    }
}
