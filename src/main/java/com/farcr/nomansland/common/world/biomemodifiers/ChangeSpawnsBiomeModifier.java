package com.farcr.nomansland.common.world.biomemodifiers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.MobSpawnSettingsBuilder;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;

import java.util.List;

public record ChangeSpawnsBiomeModifier(HolderSet<Biome> biomes, List<MobSpawnSettings.SpawnerData> spawners) implements BiomeModifier {

    public static final MapCodec<ChangeSpawnsBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Biome.LIST_CODEC.fieldOf("biomes").forGetter(ChangeSpawnsBiomeModifier::biomes),
                    MobSpawnSettings.SpawnerData.CODEC.listOf().fieldOf("spawners").forGetter(ChangeSpawnsBiomeModifier::spawners)
            ).apply(instance, ChangeSpawnsBiomeModifier::new)
    );

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.BEFORE_EVERYTHING && biomes.contains(biome)) {
            MobSpawnSettingsBuilder spawnSettingsBuilder = builder.getMobSpawnSettings();

            spawners.forEach(spawner -> {
                for (MobCategory category : MobCategory.values()) {
                    List<MobSpawnSettings.SpawnerData> spawns = spawnSettingsBuilder.getSpawner(category);
                    if (spawns.removeIf(spawnerData -> EntityType.getKey(spawnerData.type).equals(EntityType.getKey(spawner.type)))) {
                        spawnSettingsBuilder.addSpawn(spawner.type.getCategory(), spawner);
                    }
                }
            });
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
