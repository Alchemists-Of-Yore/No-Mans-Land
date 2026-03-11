package com.farcr.nomansland.common.mixin.caves;

import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin extends BiomeSource {
    @Override
    public @NotNull Set<Holder<Biome>> possibleBiomes() {
        return Stream.concat(
                super.possibleBiomes().stream(),
                Stream.of(
                        NMLBiomes.CAVES_HOLDER,
                        NMLBiomes.CAVE_DEPTHS_HOLDER
                )).collect(Collectors.toSet());
    }
}
