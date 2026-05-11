package com.farcr.nomansland.datagen.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.biomemodifiers.ChangeColorsBiomeModifier;
import com.farcr.nomansland.common.world.biomemodifiers.ChangeMusicBiomeModifier;
import com.farcr.nomansland.common.world.biomemodifiers.ChangeParticleBiomeModifier;
import com.farcr.nomansland.common.world.biomemodifiers.ChangeSpawnsBiomeModifier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.neoforged.neoforge.common.world.BiomeModifiers.*;

public class BiomeModifierBuilder {

    private final String name;
    private final HolderSet<Biome> biome;
    private final BootstrapContext<BiomeModifier> bootstrap;

    private ChangeColorsBiomeModifier changeColors = null;
    private ChangeMusicBiomeModifier changeMusic = null;
    private ChangeParticleBiomeModifier changeParticle = null;
    private final List<FeatureWithStep> addedFeatures = new ArrayList<>();
    private RemoveFeaturesBiomeModifier removeFeatures = null;
    private ChangeSpawnsBiomeModifier changeSpawns = null;
    private AddSpawnsBiomeModifier addSpawns = null;
    private RemoveSpawnsBiomeModifier removeSpawns = null;

    public BiomeModifierBuilder(BootstrapContext<BiomeModifier> bootstrap, ResourceKey<Biome> key) {
        this.bootstrap = bootstrap;
        this.name = key.location().getPath();
        this.biome = HolderSet.direct(bootstrap.lookup(Registries.BIOME).getOrThrow(key));
    }

    public final BiomeModifierBuilder changeColors(int fogColor, int waterColor, int waterFogColor, int skyColor, int grassColor, int foliageColor) {
        this.changeColors = new ChangeColorsBiomeModifier(biome, fogColor, waterColor, waterFogColor, skyColor, grassColor, foliageColor);
        return this;
    }

    public final BiomeModifierBuilder changeColors(int fogColor, int waterColor, int waterFogColor, int skyColor, int foliageColor) {
        this.changeColors = new ChangeColorsBiomeModifier(biome, fogColor, waterColor, waterFogColor, skyColor, foliageColor);
        return this;
    }

    public final BiomeModifierBuilder changeColors(int fogColor, int waterColor, int waterFogColor, int skyColor) {
        this.changeColors = new ChangeColorsBiomeModifier(biome, fogColor, waterColor, waterFogColor, skyColor);
        return this;
    }

    public final BiomeModifierBuilder changeMusic(Music music) {
        this.changeMusic = new ChangeMusicBiomeModifier(biome, music);
        return this;
    }

    public final BiomeModifierBuilder changeParticle(ParticleOptions particle, float probability) {
        this.changeParticle = new ChangeParticleBiomeModifier(biome, new AmbientParticleSettings(particle, probability));
        return this;
    }

    public final BiomeModifierBuilder addFeatures(List<FeatureWithStep> features) {
        this.addedFeatures.addAll(features);
        return this;
    }

    public final BiomeModifierBuilder addFeatures(FeatureWithStep... features) {
        this.addedFeatures.addAll(List.of(features));
        return this;
    }

    @SafeVarargs
    public final BiomeModifierBuilder removeFeatures(ResourceKey<PlacedFeature>... features) {
        List<Holder<PlacedFeature>> featureHolders = new ArrayList<>();
        List.of(features).forEach(featureKey ->
                featureHolders.add(bootstrap.lookup(Registries.PLACED_FEATURE).getOrThrow(featureKey)));

        this.removeFeatures = RemoveFeaturesBiomeModifier.allSteps(biome, HolderSet.direct(featureHolders));
        return this;
    }

    public final BiomeModifierBuilder changeSpawns(MobSpawnSettings.SpawnerData... spawners) {
        this.changeSpawns = new ChangeSpawnsBiomeModifier(biome, List.of(spawners));
        return this;
    }

    public final BiomeModifierBuilder addSpawns(MobSpawnSettings.SpawnerData... spawners) {
        this.addSpawns = new AddSpawnsBiomeModifier(biome, List.of(spawners));
        return this;
    }

    public final BiomeModifierBuilder removeSpawns(EntityType<?>... entityTypes) {
        List<Holder<EntityType<?>>> entityTypeHolders = new ArrayList<>();
        List.of(entityTypes).forEach(entityType ->
                entityTypeHolders.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entityType)));

        this.removeSpawns = new RemoveSpawnsBiomeModifier(biome, HolderSet.direct(entityTypeHolders));
        return this;
    }

    public final void build(Map<FeatureWithStep, List<Holder<Biome>>> featureToBiomes) {

        if (!addedFeatures.isEmpty()) {
            addedFeatures.forEach(feature -> featureToBiomes.computeIfAbsent(feature, f -> new ArrayList<>()).add(biome.get(0)));
        }

        if (changeColors != null) {
            bootstrap.register(
                    ResourceKey.create(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            NoMansLand.location(name + "/change_color")
                    ),
                    changeColors
            );
        }

        if (changeMusic != null) {
            bootstrap.register(
                    ResourceKey.create(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            NoMansLand.location(name + "/change_music" )
                    ),
                    changeMusic
            );
        }

        if (changeParticle != null) {
            bootstrap.register(
                    ResourceKey.create(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            NoMansLand.location(name + "/change_particle" )
                    ),
                    changeParticle
            );
        }

        if (changeSpawns != null) {
            bootstrap.register(
                    ResourceKey.create(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            NoMansLand.location(name + "/change_spawns" )
                    ),
                    changeSpawns
            );
        }

        if (addSpawns != null) {
            bootstrap.register(
                    ResourceKey.create(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            NoMansLand.location(name + "/add_spawns" )
                    ),
                    addSpawns
            );
        }

        if (removeSpawns != null) {
            bootstrap.register(
                    ResourceKey.create(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            NoMansLand.location(name + "/remove_spawns" )
                    ),
                    removeSpawns
            );
        }

        if (removeFeatures != null) {
            bootstrap.register(
                    ResourceKey.create(
                            NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                            NoMansLand.location(name + "/remove_features" )
                    ),
                    removeFeatures
            );
        }
    }
}
