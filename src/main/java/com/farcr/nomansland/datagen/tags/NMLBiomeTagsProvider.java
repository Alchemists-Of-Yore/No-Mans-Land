package com.farcr.nomansland.datagen.tags;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class NMLBiomeTagsProvider extends BiomeTagsProvider {
    public NMLBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, NoMansLand.MODID, existingFileHelper);
    }



    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BiomeTags.IS_OVERWORLD).add(
                NMLBiomes.AUTUMNAL_FOREST,
                NMLBiomes.BAYOU,
                NMLBiomes.BOG,
                NMLBiomes.DARK_SWAMP,
                NMLBiomes.DARK_TAIGA,
                NMLBiomes.MAPLE_FOREST,
                NMLBiomes.MAPLE_GROVE,
                NMLBiomes.FROZEN_WOODS,
                NMLBiomes.LUSH_RIVER,
                NMLBiomes.BLACKWATER_RIVER
        );

        tag(BiomeTags.IS_OVERWORLD).addTags(NMLTags.OLD_GROWTH_FOREST, NMLTags.CAVES);
        addToTags(NMLTags.CAVES, Tags.Biomes.IS_UNDERGROUND, Tags.Biomes.IS_CAVE);

        addToTags(NMLBiomes.AUTUMNAL_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_TEMPERATE);
        addToTags(NMLBiomes.BAYOU, BiomeTags.HAS_SWAMP_HUT, BiomeTags.IS_JUNGLE, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_DENSE_VEGETATION, Tags.Biomes.IS_HOT, Tags.Biomes.IS_JUNGLE_TREE, Tags.Biomes.IS_WET);
        addToTags(NMLBiomes.BOG, BiomeTags.HAS_SWAMP_HUT, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_COLD, Tags.Biomes.IS_WET, Tags.Biomes.IS_WET_OVERWORLD);
        addToTags(NMLBiomes.DARK_SWAMP, BiomeTags.HAS_SWAMP_HUT, BiomeTags.IS_FOREST, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_DENSE_VEGETATION, Tags.Biomes.IS_SPOOKY, Tags.Biomes.IS_TEMPERATE, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_WET, BiomeTags.HAS_WOODLAND_MANSION);
        addToTags(NMLBiomes.DARK_TAIGA, BiomeTags.IS_TAIGA, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_COLD, Tags.Biomes.IS_SPOOKY);
        addToTags(NMLBiomes.BOREAL_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_TEMPERATE);
        addToTags(NMLBiomes.MAPLE_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_TEMPERATE);
        addToTags(NMLBiomes.MAPLE_GROVE, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_COLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_PLATEAU);
        addToTags(NMLTags.OLD_GROWTH_FOREST, BiomeTags.IS_FOREST, Tags.Biomes.IS_DECIDUOUS_TREE, Tags.Biomes.IS_DENSE_VEGETATION, Tags.Biomes.IS_TEMPERATE, Tags.Biomes.IS_OLD_GROWTH, Tags.Biomes.IS_RARE);
        addToTags(NMLBiomes.FROZEN_WOODS, Tags.Biomes.IS_RARE, Tags.Biomes.IS_CONIFEROUS_TREE, Tags.Biomes.IS_COLD, Tags.Biomes.IS_SNOWY, BiomeTags.IS_TAIGA, Tags.Biomes.IS_DEAD, BiomeTags.HAS_IGLOO);

        addToTags(NMLBiomes.LUSH_RIVER, BiomeTags.IS_RIVER, BiomeTags.IS_JUNGLE, Tags.Biomes.IS_HOT, Tags.Biomes.IS_DENSE_VEGETATION, Tags.Biomes.IS_JUNGLE_TREE);
        addToTags(NMLBiomes.BLACKWATER_RIVER, BiomeTags.IS_RIVER, Tags.Biomes.IS_TEMPERATE, Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_DENSE_VEGETATION);
    }

    @SafeVarargs
    protected final void addToTags(ResourceKey<Biome> biome, TagKey<Biome>... biomeTags) {
        List.of(biomeTags).forEach(biomeTag -> tag(biomeTag).add(biome));
    }

    @SafeVarargs
    protected final void addToTags(TagKey<Biome> biome, TagKey<Biome>... biomeTags) {
        List.of(biomeTags).forEach(biomeTag -> tag(biomeTag).addTag(biome));
    }
}
