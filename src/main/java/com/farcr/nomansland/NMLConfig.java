package com.farcr.nomansland;

import net.neoforged.neoforge.common.ModConfigSpec;

public class NMLConfig {

    public static ModConfigSpec COMMON_CONFIG;
    public static final String CATEGORY_OVERRIDES = "overrides";
    public static ModConfigSpec.BooleanValue MYCELIUM_SPREADS;
    public static ModConfigSpec.BooleanValue GRASS_SPREADS;
    public static ModConfigSpec.BooleanValue MALEVOLENT_SPAWNER;
    public static ModConfigSpec.BooleanValue TRAMPLING;
    public static ModConfigSpec.BooleanValue TORCH_EXTINGUISHING;
    public static final String CATEGORY_BIOMES = "biomes";
    public static ModConfigSpec.BooleanValue BIOMES;
    public static ModConfigSpec.BooleanValue CAVES_BIOMES;
    public static ModConfigSpec.BooleanValue MAPLE_BIOMES;
    public static ModConfigSpec.BooleanValue OLD_GROWTH_FOREST;
    public static ModConfigSpec.BooleanValue AUTUMNAL_FOREST;
    public static ModConfigSpec.BooleanValue DARK_TAIGA;
    public static ModConfigSpec.BooleanValue BOREAL_FOREST;
    public static ModConfigSpec.BooleanValue DARK_SWAMP;
    public static ModConfigSpec.BooleanValue FROZEN_WOODS;
    public static ModConfigSpec.BooleanValue BAYOU;
    public static ModConfigSpec.BooleanValue BOG;
    public static ModConfigSpec.BooleanValue PRAIRIE;
    public static ModConfigSpec.BooleanValue LAVENDER_FIELD;
    public static final String CATEGORY_TAP = "tap";
    public static ModConfigSpec.DoubleValue FILLING_SPEED_MULTIPLIER;
    public static ModConfigSpec.IntValue TICKS_TO_FILL_CAULDRON;
    public static final String CATEGORY_ANCHOR = "monster_anchor";
    public static ModConfigSpec.IntValue TICKS_BETWEEN_RESURRECTIONS;
    public static final String CATEGORY_SPIKE = "spike";
    public static ModConfigSpec.DoubleValue POKING_DAMAGE;
    public static ModConfigSpec.DoubleValue FALLING_DAMAGE;
    public static ModConfigSpec.DoubleValue IMPALING_DAMAGE;
    public static ModConfigSpec.DoubleValue SKEWERING_DAMAGE;
    public static final String CATEGORY_BOMBS = "bombs";
    public static ModConfigSpec.DoubleValue EXPLOSIVE_STRENGTH;
    public static ModConfigSpec.DoubleValue FIREBOMB_STRENGTH;
    public static final String CATEGORY_BULK_PLACEMENT = "bulk_placement";
    public static ModConfigSpec.IntValue MAX_LADDER_PLACEMENT_LENGTH;
    public static ModConfigSpec.IntValue MAX_RAIL_PLACMENT_LENGTH;
    public static ModConfigSpec.IntValue MAX_FLOATING_RAILS;
    public static final String CATEGORY_MISC = "miscellaneous";
    public static ModConfigSpec.DoubleValue BURIED_SPAWNING_CHANCE;

    public static ModConfigSpec CLIENT_CONFIG;
    public static final String CATEGORY_FOG_MODIFIERS = "fog_modifiers";
    public static ModConfigSpec.BooleanValue FOG_MODIFIERS;
    public static ModConfigSpec.BooleanValue CAVE_BIOME_FOG_MODIFIER;
    public static ModConfigSpec.BooleanValue DEEP_DARK_FOG_MODIFIER;
    public static ModConfigSpec.BooleanValue FOGGY_BIOME_FOG_MODIFIER;

    static {

        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        COMMON_BUILDER.comment("For configuring the mob remodels, go to the mixed litter startup config!");

        COMMON_BUILDER.push(CATEGORY_OVERRIDES);
        GRASS_SPREADS = COMMON_BUILDER
                .comment("If grass or mycelium spread to nearby dirt.")
                .define("grassSpreads", true);
        MYCELIUM_SPREADS = COMMON_BUILDER
                .define("myceliumSpreads", true);
        MALEVOLENT_SPAWNER = COMMON_BUILDER
                .comment("If monster spawners should produce malevolent (red) flames instead of regular flames.")
                .define("malevolentSpawner", true);
        TRAMPLING = COMMON_BUILDER
                .comment("If players and mobs can trample farmland")
                .define("allowTrampling", false);
        TORCH_EXTINGUISHING = COMMON_BUILDER
                .comment("If torches can be extinguished through interactions like campfires.")
                .define("torchExtinguishing", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_BIOMES);
        BIOMES = COMMON_BUILDER
                .comment("If any custom biomes are enabled")
                .define("biomes", true);
        CAVES_BIOMES = COMMON_BUILDER
                .comment("If the generic caves biomes are enabled")
                .define("genericCavesBiomes", true);
        MAPLE_BIOMES = COMMON_BUILDER
                .comment("If the maple biomes are enabled")
                .define("mapleBiomes", true);
        OLD_GROWTH_FOREST = COMMON_BUILDER
                .comment("If the old growth forest is enabled")
                .define("oldGrowthForest", true);
        AUTUMNAL_FOREST = COMMON_BUILDER
                .comment("If the autumnal forest is enabled")
                .define("autumnal", true);
        BOREAL_FOREST = COMMON_BUILDER
                .comment("If the boreal forest is enabled")
                .define("borealForest", true);
        DARK_TAIGA = COMMON_BUILDER
                .comment("If the dark taiga is enabled")
                .define("darkTaiga", true);
        DARK_SWAMP = COMMON_BUILDER
                .comment("If the dark swamp is enabled")
                .define("darkSwamp", true);
        FROZEN_WOODS = COMMON_BUILDER
                .comment("If the frozen woods is enabled")
                .define("frozenWoods", true);
        BAYOU = COMMON_BUILDER
                .comment("If the bayou is enabled")
                .define("bayou", true);
        BOG = COMMON_BUILDER
                .comment("If the bog is enabled")
                .define("bog", true);
        PRAIRIE = COMMON_BUILDER
                .comment("If the prairie is enabled")
                .define("prairie", true);
        LAVENDER_FIELD = COMMON_BUILDER
                .comment("If the lavender field is enabled")
                .define("lavenderField", true);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_TAP);
        COMMON_BUILDER.comment("Time is calculated in ticks. 20 ticks make 1 second.");
        FILLING_SPEED_MULTIPLIER = COMMON_BUILDER
                .comment("Multiplier on how fast taps should fill up cauldrons with resin. Set to 0.0 to disable.")
                .defineInRange("fillingSpeedMultiplier", 1.0, 0, 10);
        TICKS_TO_FILL_CAULDRON = COMMON_BUILDER
                .comment("The time it takes to fill a cauldron with honey using a tap.")
                .defineInRange("ticksToFillCauldron", 80, 1, 400);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_ANCHOR);
        TICKS_BETWEEN_RESURRECTIONS = COMMON_BUILDER
                .comment("The time between each resurrection from a monster anchor.")
                .defineInRange("ticksBetweenResurrections", 80, 78, 400);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_SPIKE);
        COMMON_BUILDER.comment("Damage is dealt in hit points. 2 hit points make 1 heart.");
        POKING_DAMAGE = COMMON_BUILDER
                .comment("The continuous damage dealt to an entity as it stands on an active spike.")
                .defineInRange("pokingDamage", 1.5, 0, Integer.MAX_VALUE);
        FALLING_DAMAGE = COMMON_BUILDER
                .comment("The added damage dealt to an entity when it falls on a spike from any height.")
                .defineInRange("fallingDamage", 2.0, 0, Integer.MAX_VALUE);
        IMPALING_DAMAGE = COMMON_BUILDER
                .comment("The damage dealt to an entity when an adjacent spike is activated.")
                .defineInRange("impalingDamage", 12.0, 0, Integer.MAX_VALUE);
        SKEWERING_DAMAGE = COMMON_BUILDER
                .comment("The damage dealt to an entity when an active spike is pushed into it.")
                .defineInRange("skeweringDamage", 12.0, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_BOMBS);
        EXPLOSIVE_STRENGTH = COMMON_BUILDER
                .comment("The radius of explosives' explosion.")
                .defineInRange("explosiveExplosionRadius", 3.0, 0, Integer.MAX_VALUE);
        FIREBOMB_STRENGTH = COMMON_BUILDER
                .comment("The radius of firebombs' explosion.")
                .defineInRange("firebombExplosionRadius", 2.0, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_BULK_PLACEMENT);
        COMMON_BUILDER.comment("Set to 0 to disable any key.");
        MAX_LADDER_PLACEMENT_LENGTH = COMMON_BUILDER
                .comment("The maximum distance ladders can be placed like scaffolding.")
                .defineInRange("maxLadderPlacementLength", 8, 0, Integer.MAX_VALUE);
        MAX_RAIL_PLACMENT_LENGTH = COMMON_BUILDER
                .comment("The maximum distance rails can be placed like scaffolding.")
                .defineInRange("maxRailPlacementLength", 24, 0, Integer.MAX_VALUE);
        MAX_FLOATING_RAILS = COMMON_BUILDER
                .comment("The maximum distance rails can be from a supported block before breaking.")
                .defineInRange("maxFloatingRails", 5, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push(CATEGORY_MISC);
        BURIED_SPAWNING_CHANCE = COMMON_BUILDER
                .comment("The chance a buried is spawned upon brushing a remains block.")
                .comment("This chance is multiplied by 4 when the block is broken and by 10 when the block falls.")
                .defineInRange("buriedSpawningChance", 0.05, 0, 1);
        COMMON_BUILDER.pop();

        COMMON_CONFIG = COMMON_BUILDER.build();

        ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();

        CLIENT_BUILDER.push(CATEGORY_FOG_MODIFIERS);
        FOG_MODIFIERS = CLIENT_BUILDER
                .comment("If any custom fog modifiers are enabled")
                .define("fogModifiers", true);
        CAVE_BIOME_FOG_MODIFIER = CLIENT_BUILDER
                .comment("If the caves biome fog modifier is enabled")
                .define("cavesBiomeFogModifier", true);
        DEEP_DARK_FOG_MODIFIER = CLIENT_BUILDER
                .comment("If the deep dark fog modifier is enabled")
                .define("deepDarkFogModifier", true);
        FOGGY_BIOME_FOG_MODIFIER = CLIENT_BUILDER
                .comment("If the foggy biome fog modifier is enabled")
                .define("foggyBiomeFogModifier", true);
        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }

}
