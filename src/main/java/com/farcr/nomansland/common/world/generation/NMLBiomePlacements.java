package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.terraformersmc.biolith.api.biome.BiomePlacement;
import com.terraformersmc.biolith.api.biome.sub.BiomeParameterTargets;
import com.terraformersmc.biolith.api.biome.sub.CriterionBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;

import static com.terraformersmc.biolith.api.biome.sub.CriterionBuilder.*;

public class NMLBiomePlacements {
    public static void register() {

        /*
        if (NMLConfig.CAVES_BIOMES.get()) {
            BiomePlacement.addOverworld(NMLBiomes.CAVES,
                    Climate.parameters(
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(0.15F, 0.3F),
                            Climate.Parameter.span(-2F, 2F),
                            0.15F));

            BiomePlacement.addOverworld(NMLBiomes.CAVES,
                    Climate.parameters(
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(0.3F, 0.5F),
                            Climate.Parameter.span(-2F, 2F),
                            0.15F));

            BiomePlacement.addOverworld(NMLBiomes.CAVE_DEPTHS,
                    Climate.parameters(
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(0.6F, 0.8F),
                            Climate.Parameter.span(-2F, 2F),
                            0.15F));

            BiomePlacement.addOverworld(NMLBiomes.CAVE_DEPTHS,
                    Climate.parameters(
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            Climate.Parameter.span(0.8F, 2F),
                            Climate.Parameter.span(-2F, 2F),
                            0.1F));
        }
         */

        if (NMLConfig.MAPLE_BIOMES.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.FOREST,
                    NMLBiomes.MAPLE_FOREST,
                    0.2
            );

            BiomePlacement.replaceOverworld(
                    Biomes.GROVE,
                    NMLBiomes.MAPLE_GROVE,
                    0.2
            );

            BiomePlacement.addSubOverworld(
                    Biomes.GROVE,
                    NMLBiomes.MAPLE_GROVE,
                    alternate(NMLBiomes.MAPLE_FOREST, Biomes.FOREST)
            );

            BiomePlacement.addSubOverworld(
                    Biomes.FOREST,
                    NMLBiomes.MAPLE_FOREST,
                    alternate(NMLBiomes.MAPLE_GROVE, Biomes.GROVE)
            );

            BiomePlacement.addSubOverworld(
                    NMLBiomes.MAPLE_FOREST,
                    NMLBiomes.MAPLE_GROVE,
                    allOf(neighbor(Tags.Biomes.IS_SNOWY), not(NEAR_INTERIOR))
            );
        }

        if (NMLConfig.FROZEN_WOODS.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.SNOWY_PLAINS,
                    NMLBiomes.FROZEN_WOODS,
                    0.2
            );

            BiomePlacement.replaceOverworld(
                    Biomes.SNOWY_TAIGA,
                    NMLBiomes.FROZEN_WOODS,
                    0.2
            );

            BiomePlacement.addSubOverworld(
                    Biomes.SNOWY_TAIGA,
                    NMLBiomes.FROZEN_WOODS,
                    alternate(NMLBiomes.FROZEN_WOODS, Biomes.SNOWY_PLAINS)
            );

            BiomePlacement.addSubOverworld(
                    Biomes.SNOWY_PLAINS,
                    NMLBiomes.FROZEN_WOODS,
                    alternate(NMLBiomes.FROZEN_WOODS, Biomes.SNOWY_TAIGA)
            );
        }

        if (NMLConfig.OLD_GROWTH_FOREST.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.DARK_FOREST,
                    NMLBiomes.OLD_GROWTH_FOREST,
                    0.3
            );

            BiomePlacement.replaceOverworld(
                    Biomes.OLD_GROWTH_BIRCH_FOREST,
                    NMLBiomes.OLD_GROWTH_FOREST,
                    0.2
            );

            edgeBiome(NMLBiomes.OLD_GROWTH_FOREST, NMLBiomes.OLD_GROWTH_FOREST_EDGE, BiomeParameterTargets.HUMIDITY);
            clearingBiome(NMLBiomes.OLD_GROWTH_FOREST, NMLBiomes.OLD_GROWTH_FOREST_CLEARING);
        }

        if (NMLConfig.AUTUMNAL_FOREST.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.FOREST,
                    NMLBiomes.AUTUMNAL_FOREST,
                    0.2
            );
        }

        if (NMLConfig.BOREAL_FOREST.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.TAIGA,
                    NMLBiomes.BOREAL_FOREST,
                    0.2
            );

            BiomePlacement.replaceOverworld(
                    Biomes.FOREST,
                    NMLBiomes.BOREAL_FOREST,
                    0.1
            );

            transitionalBiome(
                    Biomes.TAIGA,
                    NMLBiomes.AUTUMNAL_FOREST,
                    NMLBiomes.BOREAL_FOREST
            );
        }

        if (NMLConfig.DARK_TAIGA.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.TAIGA,
                    NMLBiomes.DARK_TAIGA,
                    0.3
            );
        }

        if (NMLConfig.DARK_SWAMP.get()) {
            transitionalBiome(
                    Biomes.SWAMP,
                    Biomes.DARK_FOREST,
                    NMLBiomes.DARK_SWAMP
            );

            BiomePlacement.replaceOverworld(
                    Biomes.DARK_FOREST,
                    NMLBiomes.DARK_SWAMP,
                    0.1
            );
            BiomePlacement.replaceOverworld(
                    Biomes.SWAMP,
                    NMLBiomes.DARK_SWAMP,
                    0.2
            );
        }

        if (NMLConfig.BAYOU.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.MANGROVE_SWAMP,
                    NMLBiomes.BAYOU,
                    0.4
            );

            BiomePlacement.replaceOverworld(
                    Biomes.JUNGLE,
                    NMLBiomes.BAYOU,
                    0.1
            );

            transitionalBiome(
                    Biomes.SWAMP,
                    Biomes.JUNGLE,
                    NMLBiomes.BAYOU
            );
        }

        if (NMLConfig.BOG.get()) {
            BiomePlacement.replaceOverworld(
                    Biomes.SWAMP,
                    NMLBiomes.BOG,
                    0.2
            );

            BiomePlacement.replaceOverworld(
                    Biomes.MANGROVE_SWAMP,
                    NMLBiomes.BOG,
                    0.05
            );
            BiomePlacement.addSubOverworld(
                    Biomes.SWAMP,
                    NMLBiomes.BOG,
                    neighbor(Biomes.PLAINS)
            );
        }
    }

    public static void transitionalBiome(ResourceKey<Biome> mainBiome, ResourceKey<Biome> secondaryBiome, ResourceKey<Biome> transitionalBiome) {
        BiomePlacement.addSubOverworld(
                mainBiome,
                transitionalBiome,
                allOf(neighbor(secondaryBiome), not(NEAR_INTERIOR))
        );

        BiomePlacement.addSubOverworld(
                secondaryBiome,
                transitionalBiome,
                allOf(alternate(transitionalBiome, mainBiome), not(NEAR_INTERIOR))
        );
    }

    public static void edgeBiome(ResourceKey<Biome> interiorBiome, ResourceKey<Biome> edgeBiome, BiomeParameterTargets biomeParameterTarget) {
        BiomePlacement.addSubOverworld(
                interiorBiome,
                edgeBiome,
                allOf(
                        CriterionBuilder.deviationMin(biomeParameterTarget, .02F),
                        anyOf(
                                allOf(
                                        NEAR_BORDER,
                                        not(NEAR_INTERIOR)
                                ),
                                CriterionBuilder.BEACHSIDE,
                                CriterionBuilder.OCEANSIDE,
                                allOf(NEAR_BORDER, neighbor(BiomeTags.IS_RIVER))
                        )
                )
        );
    }

    public static void clearingBiome(ResourceKey<Biome> interiorBiome, ResourceKey<Biome> clearingBiome) {
        BiomePlacement.addSubOverworld(
                interiorBiome,
                clearingBiome,
                allOf(CriterionBuilder.deviationMin(BiomeParameterTargets.WEIRDNESS, .02F), NEAR_INTERIOR, not(NEAR_BORDER))
        );
    }
}
