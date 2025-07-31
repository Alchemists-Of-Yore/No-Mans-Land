package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.definitions.SharedTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public class NMLTags {
    public static final TagKey<Item> FIRESTARTERS = createItemTag("firestarters");
    public static final TagKey<Item> DEER_FOOD = createItemTag("deer_food");
    public static final TagKey<Item> MOOSE_FOOD = createItemTag("moose_food");
    public static final TagKey<Item> TORTOISE_FOOD = createItemTag("tortoise_food");
    public static final TagKey<Item> MAKES_RESIN_OIL = createItemTag("makes_resin_oil");
    public static final TagKey<Item> INK_IMMUNE = createItemTag("ink_immune");

    public static final SharedTag MAPLE_LOGS = createSharedTag("maple_logs");
    public static final SharedTag PINE_LOGS = createSharedTag("pine_logs");
    public static final SharedTag WALNUT_LOGS = createSharedTag("walnut_logs");
    public static final SharedTag WILLOW_LOGS = createSharedTag("willow_logs");
    public static final SharedTag BOOKSHELVES = createSharedTag("bookshelves");
    public static final SharedTag TRIMMED_PLANKS = createSharedTag("trimmed_planks");

    public static final TagKey<Block> MUSHROOM_BLOCKS = createBlockTag("mushroom_blocks");
    public static final TagKey<Block> BONEMEALABLE_FLOWERS = createBlockTag("bonemealable_flowers");
    public static final TagKey<Biome> HAS_DENSE_FOG = createBiomeTag("has_dense_fog");
    public static final TagKey<Biome> HAS_CACTUS = createBiomeTag("has_cactus");
    public static final TagKey<Block> BOMB_EXPLODE = createBlockTag("firebomb_explode");
    public static final TagKey<Block> HEAT_SOURCES = createBlockTag("heat_sources");
    public static final TagKey<EntityType<?>> ANCHOR_BLACKLIST = createEntityTag("anchor_blacklist");
    public static final TagKey<Biome> OLD_GROWTH_FOREST = createBiomeTag("old_growth_forest");

    public static final TagKey<Biome> CAVES = createBiomeTag("caves");
    public static final TagKey<DamageType> IGNITES_FLAMMABLE = createDamageTypeTag("ignites_flammable");
    public static final TagKey<Biome> SPAWNS_MUD_FROGS = createBiomeTag("entity/spawns_mud_frogs");

    public static final TagKey<Block> SUPPORTS_ICICLE = createBlockTag("supports_icicle");

    public static final TagKey<Biome> CLASSIC_SOIL = createBiomeTag("classic_soil");
    public static final TagKey<Biome> IS_SHORELINE = createBiomeTag("is_shoreline");
    public static final TagKey<Biome> IS_CRAGLAND = createBiomeTag("is_cragland");
    public static final TagKey<Block> SUITABLE_TORTOISE_HOME = createBlockTag("suitable_tortoise_home");

    public static class FeatureAddition {
        // Broad brushes
        public static final TagKey<Biome> HAS_OVERWORLD_FOLIAGE = createBiomeTag("feature_addition/has_overworld_foliage");
        public static final TagKey<Biome> HAS_SWAMP_FOLIAGE = createBiomeTag("feature_addition/has_swamp_foliage");

        // Specific foliage patches
        public static final TagKey<Biome> HAS_BEACHGRASS = createBiomeTag("feature_addition/has_beachgrass");
        public static final TagKey<Biome> HAS_CAVE_WEEDS = createBiomeTag("feature_addition/has_cave_weeds");
        public static final TagKey<Biome> HAS_CLOVERS = createBiomeTag("feature_addition/has_clovers");
        public static final TagKey<Biome> HAS_DRIED_GRASS = createBiomeTag("feature_addition/has_dried_grass");
        public static final TagKey<Biome> HAS_FERN_PATCH = createBiomeTag("feature_addition/has_fern_patch");
        public static final TagKey<Biome> HAS_FIDDLEHEAD = createBiomeTag("feature_addition/has_fiddlehead");
        public static final TagKey<Biome> HAS_FIELD_MUSHROOM = createBiomeTag("feature_addition/has_field_mushroom");
        public static final TagKey<Biome> HAS_FROSTED_GRASS_FOREST = createBiomeTag("feature_addition/has_frosted_grass_forest");
        public static final TagKey<Biome> HAS_FROSTED_GRASS_PLAINS = createBiomeTag("feature_addition/has_frosted_grass_plains");
        public static final TagKey<Biome> HAS_ICICLES = createBiomeTag("feature_addition/has_icicles");
        public static final TagKey<Biome> HAS_OAT_GRASS = createBiomeTag("feature_addition/has_oat_grass");
        public static final TagKey<Biome> HAS_RAFFLESIA = createBiomeTag("feature_addition/has_rafflesia");
        public static final TagKey<Biome> HAS_GROUND_IVY = createBiomeTag("feature_addition/has_ground_ivy");

        // Fallen trees
        public static final TagKey<Biome> HAS_FALLEN_TREES_DRY = createBiomeTag("feature_addition/has_fallen_trees_dry");
        public static final TagKey<Biome> HAS_FALLEN_TREES_FOREST = createBiomeTag("feature_addition/has_fallen_trees_forest");
        public static final TagKey<Biome> HAS_FALLEN_TREES_JUNGLE = createBiomeTag("feature_addition/has_fallen_trees_jungle");
        public static final TagKey<Biome> HAS_FALLEN_TREES_SNOWY = createBiomeTag("feature_addition/has_fallen_trees_snowy");
        public static final TagKey<Biome> HAS_FALLEN_TREES_SPARSE = createBiomeTag("feature_addition/has_fallen_trees_sparse");

        // Rocks & terrain
        public static final TagKey<Biome> HAS_PEBBLES = createBiomeTag("feature_addition/has_pebbles");
        public static final TagKey<Biome> HAS_FOREST_ROCK = createBiomeTag("feature_addition/has_forest_rock");
        public static final TagKey<Biome> HAS_STONE_BOULDER = createBiomeTag("feature_addition/has_stone_boulder");
        public static final TagKey<Biome> HAS_POND_COMMON = createBiomeTag("feature_addition/has_pond_common");
        public static final TagKey<Biome> HAS_POND_COMMON_SNOWY = createBiomeTag("feature_addition/has_pond_common_snowy");
        public static final TagKey<Biome> HAS_POND_SWAMP = createBiomeTag("feature_addition/has_pond_swamp");
        public static final TagKey<Biome> HAS_QUARTZITE_GEODE = createBiomeTag("feature_addition/has_quartzite_geode");
        public static final TagKey<Biome> HAS_NETHER_QUARTZITE_GEODE = createBiomeTag("feature_addition/has_nether_quartzite_geode");
        public static final TagKey<Biome> HAS_MUD_PATCH = createBiomeTag("feature_addition/has_mud_patch");

        // Extra mushroom stuff
        public static final TagKey<Biome> HAS_FIELD_MUSHROOM_CIRCLE = createBiomeTag("feature_addition/has_field_mushroom_circle");
        public static final TagKey<Biome> HAS_SHELF_MUSHROOM_FOREST = createBiomeTag("feature_addition/has_shelf_mushroom_forest");
        public static final TagKey<Biome> HAS_SHELF_MUSHROOM_OLD_GROWTH = createBiomeTag("feature_addition/has_shelf_mushroom_old_growth");
        public static final TagKey<Biome> HAS_SHELF_MUSHROOM_TAIGA = createBiomeTag("feature_addition/has_shelf_mushroom_taiga");
    }

    public static class FeatureRemoval {
        public static final TagKey<Biome> NO_CATTAIL = createBiomeTag("feature_removal/no_cattail");
    }

    private static SharedTag createSharedTag(String name) {
        return new SharedTag(name);
    }

    private static TagKey<Item> createItemTag(String name) {
        return TagKey.create(Registries.ITEM, NoMansLand.location(name));
    }

    private static TagKey<Block> createBlockTag(String name) {
        return TagKey.create(Registries.BLOCK, NoMansLand.location(name));
    }

    private static TagKey<Biome> createBiomeTag(String name) {
        return TagKey.create(Registries.BIOME, NoMansLand.location(name));
    }

    private static TagKey<EntityType<?>> createEntityTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, NoMansLand.location(name));
    }

    private static TagKey<DamageType> createDamageTypeTag(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, NoMansLand.location(name));
    }
}
