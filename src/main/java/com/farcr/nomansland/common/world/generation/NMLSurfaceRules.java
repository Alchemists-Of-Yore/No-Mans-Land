package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.farcr.nomansland.common.world.surfacerule.BelowOrEqualToYConditionSource;
import com.terraformersmc.biolith.api.surface.SurfaceGeneration;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class NMLSurfaceRules {
    private static final SurfaceRules.RuleSource COARSE_DIRT = makeStateRule(Blocks.COARSE_DIRT);
    private static final SurfaceRules.RuleSource PODZOL = makeStateRule(Blocks.PODZOL);
    private static final SurfaceRules.RuleSource MUD = makeStateRule(Blocks.MUD);
    private static final SurfaceRules.RuleSource SILT = makeStateRule(NMLBlocks.SILT.get());
    private static final SurfaceRules.RuleSource WATER = makeStateRule(Blocks.WATER);
    private static final SurfaceRules.RuleSource GRAVEL = makeStateRule(Blocks.GRAVEL);
    private static final SurfaceRules.RuleSource SAND = makeStateRule(Blocks.SAND);
    private static final SurfaceRules.RuleSource SNOW_BLOCK = makeStateRule(Blocks.SNOW_BLOCK);
    private static final SurfaceRules.RuleSource PACKED_ICE = makeStateRule(Blocks.PACKED_ICE);
    private static final SurfaceRules.RuleSource ICE = makeStateRule(Blocks.ICE);
    private static final SurfaceRules.RuleSource SANDSTONE_UNDER_SAND = SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SAND),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.SAND.defaultBlockState())),
            SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, SurfaceRules.state(Blocks.SANDSTONE.defaultBlockState()))
    );

    private static final SurfaceRules.RuleSource DEEP_GRAVEL = SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, GRAVEL),
            SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())),
            SurfaceRules.ifTrue(SurfaceRules.DEEP_UNDER_FLOOR, SurfaceRules.state(Blocks.GRAVEL.defaultBlockState()))
    );

    private static final SurfaceRules.ConditionSource BEACH = new BelowOrEqualToYConditionSource(VerticalAnchor.absolute(70), true, true);

    public static void register() {

        SurfaceRules.RuleSource jungle = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.JUNGLE),
                SurfaceRules.ifTrue(surfaceNoiseAbove(1.25), COARSE_DIRT)
        );

        SurfaceRules.RuleSource darkForest = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.DARK_FOREST),
                SurfaceRules.ifTrue(surfaceNoiseAbove(1.25), PODZOL)
        );

        SurfaceRules.RuleSource autumnalForest = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.AUTUMNAL_FOREST),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), COARSE_DIRT),
                        SurfaceRules.ifTrue(surfaceNoiseAbove(-0.95), PODZOL))
        );

        SurfaceRules.RuleSource mapleForest = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.MAPLE_FOREST),
                SurfaceRules.ifTrue(surfaceNoiseAbove(1.25), PODZOL)
        );

        SurfaceRules.RuleSource oldGrowthForest = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.OLD_GROWTH_FOREST),
                SurfaceRules.ifTrue(surfaceNoiseAbove(1.25), COARSE_DIRT)
        );

        SurfaceRules.RuleSource frozenWoods = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.FROZEN_WOODS),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surfaceNoiseAbove(2.25), SNOW_BLOCK),
                        SurfaceRules.ifTrue(surfaceNoiseAbove(1.5), MUD),
                        SurfaceRules.ifTrue(surfaceNoiseAbove(1.0), SILT)
                )
        );

        SurfaceRules.RuleSource bog = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.BOG),
                        SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0),
                                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0)),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER)))
        );

        SurfaceRules.RuleSource bayou = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.BAYOU),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surfaceNoiseAbove(2.0), MUD), SurfaceRules.ifTrue(surfaceNoiseAbove(1.75), PODZOL),
                        SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0),
                                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0)),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        SurfaceRules.RuleSource darkSwamp = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.DARK_SWAMP),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surfaceNoiseAbove(1.25), PODZOL),
                        SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0),
                                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0)),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        SurfaceRules.RuleSource stonyShore = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(Biomes.STONY_SHORE),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surfaceNoiseAbove(1.9), SILT),
                        SurfaceRules.ifTrue(surfaceNoiseAbove(1.7), SurfaceRules.sequence(
                                SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), SILT),
                                GRAVEL
                        )),
                        SurfaceRules.ifTrue(surfaceNoiseAbove(-0.45), GRAVEL),
                        SurfaceRules.ifTrue(surfaceNoiseAbove(-0.95), SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), GRAVEL)))
        );

        SurfaceRules.RuleSource lush_river = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.LUSH_RIVER),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surfaceNoiseAbove(1.25), COARSE_DIRT),
                        SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0),
                                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0)),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        SurfaceRules.RuleSource blackwater_river = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.BLACKWATER_RIVER),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(surfaceNoiseAbove(-0.75), MUD),
                        SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(62), 0),
                                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(63), 0)),
                                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SWAMP, 0.0), WATER))))
        );

        SurfaceRules.RuleSource desert_river = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.DESERT_RIVER),
                SANDSTONE_UNDER_SAND
        );

        SurfaceRules.RuleSource mud_beach = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.MUD_BEACH),
                SurfaceRules.ifTrue(
                        BEACH,
                        SurfaceRules.ifTrue(
                                SurfaceRules.UNDER_FLOOR,
                                SurfaceRules.state(Blocks.MUD.defaultBlockState())
                        )
                )
        );
        SurfaceRules.RuleSource frozen_beach = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.FROZEN_BEACH),
                SurfaceRules.ifTrue(
                        BEACH,
                        SurfaceRules.ifTrue(
                                SurfaceRules.UNDER_FLOOR,
                                SurfaceRules.state(Blocks.GRAVEL.defaultBlockState())
                        )
                )
        );
        SurfaceRules.RuleSource tropical_beach = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.TROPICAL_BEACH),
                SurfaceRules.ifTrue(BEACH, SANDSTONE_UNDER_SAND)
        );
        SurfaceRules.RuleSource gravel_beach = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.GRAVEL_BEACH),
                DEEP_GRAVEL
        );

        SurfaceRules.RuleSource caves = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.CAVES),
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, true, CaveSurface.FLOOR),
                        SurfaceRules.state(Blocks.STONE.defaultBlockState())
                )
        );

        SurfaceRules.RuleSource cave_depths = SurfaceRules.ifTrue(
                SurfaceRules.isBiome(NMLBiomes.CAVE_DEPTHS),
                SurfaceRules.ifTrue(SurfaceRules.stoneDepthCheck(0, true, CaveSurface.FLOOR),
                        SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState())
                )
        );

        SurfaceGeneration.addOverworldSurfaceRules(
                NoMansLand.location("rules/overworld"),
                // Surface Biomes
                SurfaceRules.ifTrue(
                        SurfaceRules.abovePreliminarySurface(),
                        SurfaceRules.sequence(
                            // top layer biome modifiers - grasses, etc.
                            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                                    SurfaceRules.sequence(jungle, darkForest, autumnalForest, mapleForest, oldGrowthForest, frozenWoods, bog, bayou, darkSwamp, stonyShore, lush_river, blackwater_river)
                            ),
                            // deeper layer biome modifiers - sand, beaches...
                            SurfaceRules.sequence(desert_river, mud_beach, frozen_beach, tropical_beach, gravel_beach)
                        )
                ),
                // Cave Biomes
                SurfaceRules.ifTrue(
                        SurfaceRules.not(SurfaceRules.abovePreliminarySurface()),
                        SurfaceRules.sequence(caves, cave_depths)
                )
        );
    }

    private static SurfaceRules.RuleSource makeStateRule(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    private static SurfaceRules.ConditionSource surfaceNoiseAbove(double value) {
        return SurfaceRules.noiseCondition(Noises.SURFACE, value / 8.25, Double.MAX_VALUE);
    }
}