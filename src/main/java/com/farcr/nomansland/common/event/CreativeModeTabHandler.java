package com.farcr.nomansland.common.event;

import com.farcr.nomansland.common.definitions.ItemLikeDefinition;
import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import static com.farcr.nomansland.common.registry.blocks.NMLBlocks.*;
import static com.farcr.nomansland.common.registry.items.NMLItems.*;

@SuppressWarnings("unused")
public class CreativeModeTabHandler {

    private BuildCreativeModeTabContentsEvent event;

    private void insertBefore(Item existingEntry, ItemLikeDefinition<?, ?> newEntry) {
        ItemStack existingStack = existingEntry.getDefaultInstance();
        ItemStack newStack = newEntry.stack();
        event.insertBefore(existingStack, newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    private void insertAfter(Item existingEntry, ItemLikeDefinition<?, ?> newEntry) {
        ItemStack existingStack = existingEntry.getDefaultInstance();
        ItemStack newStack = newEntry.stack();
        event.insertAfter(existingStack, newStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    @SubscribeEvent
    public void onBuildCreativeModeTabContents(final BuildCreativeModeTabContentsEvent event) {
        this.event = event;
        ResourceKey<CreativeModeTab> tab = event.getTabKey();

        //Note about the methods "insertBefore and insertAfter"
        //insertAfter reads from the bottom up, while insertBefore reads from up to bottom.
        //Might look messy, but trust me it makes sense I swear. 
        //-Farcr
        
        if (tab == CreativeModeTabs.BUILDING_BLOCKS) {
            insertAfter(Items.STONE_BRICKS, FADED_STONE_BRICKS);
            insertBefore(Items.STONE_BRICKS, POLISHED_STONE);
            insertBefore(Items.STONE_BRICKS, POLISHED_STONE_STAIRS);
            insertBefore(Items.STONE_BRICKS, POLISHED_STONE_SLAB);

            insertAfter(Items.MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICK_WALL);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICK_SLAB);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICK_STAIRS);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, MOSSY_COBBLESTONE_BRICKS);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, CRACKED_COBBLESTONE_BRICKS);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICK_WALL);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICK_SLAB);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICK_STAIRS);
            insertAfter(Items.MOSSY_COBBLESTONE_WALL, COBBLESTONE_BRICKS);

            insertAfter(Items.SMOOTH_STONE_SLAB, MUNDANE_TILE_SLAB);
            insertAfter(Items.SMOOTH_STONE_SLAB, MUNDANE_TILE_STAIRS);
            insertAfter(Items.SMOOTH_STONE_SLAB, MUNDANE_TILES);
            insertBefore(Items.PACKED_MUD, EARTHEN_TILES);
            insertBefore(Items.PACKED_MUD, EARTHEN_TILE_STAIRS);
            insertBefore(Items.PACKED_MUD, EARTHEN_TILE_SLAB);
            insertBefore(Items.PACKED_MUD, DROSS_TILES);
            insertBefore(Items.PACKED_MUD, DROSS_TILE_STAIRS);
            insertBefore(Items.PACKED_MUD, DROSS_TILE_SLAB);
            insertBefore(Items.PACKED_MUD, SILT_BRICKS);
            insertBefore(Items.PACKED_MUD, SILT_BRICK_STAIRS);
            insertBefore(Items.PACKED_MUD, SILT_BRICK_SLAB);

            insertAfter(Items.BRICK_WALL, MOSSY_COARSE_BRICK_WALL);
            insertAfter(Items.BRICK_WALL, MOSSY_COARSE_BRICK_SLAB);
            insertAfter(Items.BRICK_WALL, MOSSY_COARSE_BRICK_STAIRS);
            insertAfter(Items.BRICK_WALL, MOSSY_COARSE_BRICKS);
            insertAfter(Items.BRICK_WALL, COARSE_BRICK_WALL);
            insertAfter(Items.BRICK_WALL, COARSE_BRICK_SLAB);
            insertAfter(Items.BRICK_WALL, COARSE_BRICK_STAIRS);
            insertAfter(Items.BRICK_WALL, COARSE_BRICKS);

            insertAfter(Items.SPRUCE_BUTTON, PINE.button());
            insertAfter(Items.SPRUCE_BUTTON, PINE.pressurePlate());
            insertAfter(Items.SPRUCE_BUTTON, PINE.trapdoor());
            insertAfter(Items.SPRUCE_BUTTON, PINE.door());
            insertAfter(Items.SPRUCE_BUTTON, PINE.fenceGate());
            insertAfter(Items.SPRUCE_BUTTON, PINE.fence());
            if (Mods.FARMERSDELIGHT.isLoaded()) insertAfter(Items.SPRUCE_BUTTON, FDIntegration.PINE_CABINET);
            insertAfter(Items.SPRUCE_BUTTON, PINE.trimmedPlanks());
            insertAfter(Items.SPRUCE_BUTTON, PINE.bookshelf());
            insertAfter(Items.SPRUCE_BUTTON, PINE.slab());
            insertAfter(Items.SPRUCE_BUTTON, PINE.stairs());
            insertAfter(Items.SPRUCE_BUTTON, PINE.planks());
            insertAfter(Items.SPRUCE_BUTTON, PINE.strippedWood());
            insertAfter(Items.SPRUCE_BUTTON, PINE.strippedLog());
            insertAfter(Items.SPRUCE_BUTTON, PINE.wood());
            insertAfter(Items.SPRUCE_BUTTON, PINE.log());

            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.button());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.pressurePlate());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.trapdoor());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.door());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.fenceGate());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.fence());
            if (Mods.FARMERSDELIGHT.isLoaded()) insertAfter(Items.DARK_OAK_BUTTON, FDIntegration.WALNUT_CABINET);
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.trimmedPlanks());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.bookshelf());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.slab());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.stairs());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.planks());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.strippedWood());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.strippedLog());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.wood());
            insertAfter(Items.DARK_OAK_BUTTON, WALNUT.log());

            insertBefore(Items.DARK_OAK_LOG, MAPLE.log());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.wood());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.strippedLog());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.strippedWood());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.planks());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.stairs());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.slab());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.bookshelf());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.trimmedPlanks());
            if (Mods.FARMERSDELIGHT.isLoaded()) insertBefore(Items.DARK_OAK_LOG, FDIntegration.MAPLE_CABINET);
            insertBefore(Items.DARK_OAK_LOG, MAPLE.fence());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.fenceGate());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.door());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.trapdoor());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.pressurePlate());
            insertBefore(Items.DARK_OAK_LOG, MAPLE.button());

            insertBefore(Items.MANGROVE_LOG, WILLOW.log());
            insertBefore(Items.MANGROVE_LOG, WILLOW.wood());
            insertBefore(Items.MANGROVE_LOG, WILLOW.strippedLog());
            insertBefore(Items.MANGROVE_LOG, WILLOW.strippedWood());
            insertBefore(Items.MANGROVE_LOG, WILLOW.planks());
            insertBefore(Items.MANGROVE_LOG, WILLOW.stairs());
            insertBefore(Items.MANGROVE_LOG, WILLOW.slab());
            insertBefore(Items.MANGROVE_LOG, WILLOW.bookshelf());
            insertBefore(Items.MANGROVE_LOG, WILLOW.trimmedPlanks());
            if (Mods.FARMERSDELIGHT.isLoaded()) insertBefore(Items.MANGROVE_LOG, FDIntegration.WILLOW_CABINET);
            insertBefore(Items.MANGROVE_LOG, WILLOW.fence());
            insertBefore(Items.MANGROVE_LOG, WILLOW.fenceGate());
            insertBefore(Items.MANGROVE_LOG, WILLOW.door());
            insertBefore(Items.MANGROVE_LOG, WILLOW.trapdoor());
            insertBefore(Items.MANGROVE_LOG, WILLOW.pressurePlate());
            insertBefore(Items.MANGROVE_LOG, WILLOW.button());

            insertAfter(Items.OAK_SLAB, TRIMMED_OAK_PLANKS);
            event.insertAfter(Items.OAK_SLAB.getDefaultInstance(), Items.BOOKSHELF.getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            insertAfter(Items.SPRUCE_SLAB, TRIMMED_SPRUCE_PLANKS);
            insertAfter(Items.SPRUCE_SLAB, SPRUCE_BOOKSHELF);
            insertAfter(Items.BIRCH_SLAB, TRIMMED_BIRCH_PLANKS);
            insertAfter(Items.BIRCH_SLAB, BIRCH_BOOKSHELF);
            insertAfter(Items.JUNGLE_SLAB, TRIMMED_JUNGLE_PLANKS);
            insertAfter(Items.JUNGLE_SLAB, JUNGLE_BOOKSHELF);
            insertAfter(Items.ACACIA_SLAB, TRIMMED_ACACIA_PLANKS);
            insertAfter(Items.ACACIA_SLAB, ACACIA_BOOKSHELF);
            insertAfter(Items.DARK_OAK_SLAB, TRIMMED_DARK_OAK_PLANKS);
            insertAfter(Items.DARK_OAK_SLAB, DARK_OAK_BOOKSHELF);
            insertAfter(Items.CHERRY_SLAB, TRIMMED_CHERRY_PLANKS);
            insertAfter(Items.CHERRY_SLAB, CHERRY_BOOKSHELF);
            insertAfter(Items.MANGROVE_SLAB, TRIMMED_MANGROVE_PLANKS);
            insertAfter(Items.MANGROVE_SLAB, MANGROVE_BOOKSHELF);
            insertAfter(Items.CRIMSON_SLAB, TRIMMED_CRIMSON_PLANKS);
            insertAfter(Items.CRIMSON_SLAB, CRIMSON_BOOKSHELF);
            insertAfter(Items.WARPED_SLAB, TRIMMED_WARPED_PLANKS);
            insertAfter(Items.WARPED_SLAB, WARPED_BOOKSHELF);
            insertAfter(Items.BAMBOO_MOSAIC_SLAB, TRIMMED_BAMBOO_PLANKS);
            insertAfter(Items.BAMBOO_MOSAIC_SLAB, BAMBOO_BOOKSHELF);

            insertBefore(Items.AMETHYST_BLOCK, QUARTZITE);
        }

//        if (tab == CreativeModeTabs.COLORED_BLOCKS) {
//        }

        if (tab == CreativeModeTabs.NATURAL_BLOCKS) {
            insertAfter(Items.SHORT_GRASS, NMLItems.FROSTED_GRASS);
            insertAfter(Items.SHORT_GRASS, OAT_GRASS);
            insertAfter(Items.SHORT_GRASS, GRASS_SPROUTS);
            insertAfter(Items.FERN, FIDDLEHEAD);
            insertBefore(Items.DEAD_BUSH, SHORT_BEACHGRASS);
            insertBefore(Items.DEAD_BUSH, TALL_BEACHGRASS);
            insertBefore(Items.DEAD_BUSH, CAVE_WEEDS);
            insertBefore(Items.DEAD_BUSH, DRIED_GRASS);
            insertBefore(Items.CRIMSON_ROOTS, MYCELIUM_GROWTHS);
            insertBefore(Items.CRIMSON_ROOTS, MYCELIUM_SPROUTS);
            insertAfter(Items.LARGE_FERN, CATTAIL);
            insertAfter(Items.LARGE_FERN, REEDS);
            insertAfter(Items.LILY_PAD, NMLItems.DUCKWEED);
            insertAfter(Items.LILY_PAD, NMLItems.WATER_MOSAIC);
            insertAfter(Items.HANGING_ROOTS, BEARD_MOSS);
            insertAfter(Items.PINK_PETALS, CLOVER_PATCH);
            insertAfter(Items.PINK_PETALS, RED_FLOWERBED);
            insertAfter(Items.PINK_PETALS, YELLOW_FLOWERBED);
            insertAfter(Items.PINK_PETALS, BLUE_FLOWERBED);
            insertAfter(Items.PINK_PETALS, VIOLET_FLOWERBED);
            insertAfter(Items.PINK_PETALS, WHITE_FLOWERBED);
            insertAfter(Items.LILY_OF_THE_VALLEY, LAVENDER_BUSH);
            insertAfter(Items.LILY_OF_THE_VALLEY, RED_LUPINE);
            insertAfter(Items.LILY_OF_THE_VALLEY, BLUE_LUPINE);
            insertAfter(Items.LILY_OF_THE_VALLEY, PINK_LUPINE);
            insertAfter(Items.LILY_OF_THE_VALLEY, YELLOW_LUPINE);
            insertAfter(Items.LILY_OF_THE_VALLEY, ACONITE);
            insertAfter(Items.LILY_OF_THE_VALLEY, WILD_MINT);
            insertAfter(Items.LILY_OF_THE_VALLEY, AUTUMN_CROCUS);
            insertAfter(Items.LILY_OF_THE_VALLEY, THISTLE);
            insertAfter(Items.LILY_OF_THE_VALLEY, STARFLOWER);
            insertAfter(Items.SPORE_BLOSSOM, RAFFLESIA);
            insertAfter(Items.DEAD_BUSH, BARREL_CACTUS);
            insertAfter(Items.DEAD_BUSH, SUCCULENT);
            insertAfter(Items.DEAD_BUSH, PICKLEWEED);
            insertAfter(Items.WARPED_FUNGUS, PEBBLES);
            insertAfter(Items.WARPED_FUNGUS, SEASHELLS);
            insertAfter(Items.BIRCH_LEAVES, YELLOW_BIRCH_LEAVES);
            insertAfter(Items.BIRCH_SAPLING, YELLOW_BIRCH_SAPLING);
            insertAfter(Items.OAK_LEAVES, AUTUMNAL_OAK_LEAVES);
            insertAfter(Items.OAK_SAPLING, AUTUMNAL_OAK_SAPLING);
            insertAfter(Items.CHERRY_LEAVES, PALE_CHERRY_LEAVES);
            insertAfter(Items.CHERRY_SAPLING, PALE_CHERRY_SAPLING);
            insertBefore(Items.GRAVEL, SILT);
            insertBefore(Items.GRAVEL, COARSE_SILT);
            insertBefore(Items.GRAVEL, SILT_PATH);
            insertAfter(Items.RED_MUSHROOM, SHELF_MUSHROOM);
            insertAfter(Items.RED_MUSHROOM, NMLItems.FIELD_MUSHROOM);
            if (Mods.FARMERSDELIGHT.isLoaded()) insertAfter(Items.RED_MUSHROOM, FDIntegration.FIELD_MUSHROOM_COLONY);
            insertAfter(Items.RED_MUSHROOM_BLOCK, SHELF_MUSHROOM_BLOCK);
            insertAfter(Items.RED_MUSHROOM_BLOCK, FIELD_MUSHROOM_BLOCK);
            insertAfter(Items.SPRUCE_LEAVES, FROSTED_LEAVES);
            insertAfter(Items.SAND, SAND_PATH);
            insertAfter(Items.RED_SAND, RED_SAND_PATH);
            insertAfter(Items.SNOW_BLOCK, SNOW_PATH);
            insertAfter(Items.GRAVEL, GRAVEL_PATH);
            insertAfter(Items.SNOW, SNOWY_GRASS_PATH);
            insertAfter(Items.MYCELIUM, MYCELIUM_PATH);
            insertAfter(Items.PODZOL, PODZOL_PATH);
            insertBefore(Items.DIRT, DIRT_PATH);
            insertAfter(Items.AMETHYST_CLUSTER, QUARTZITE_CLUSTER);
            insertAfter(Items.AMETHYST_CLUSTER, LARGE_QUARTZITE_BUD);
            insertAfter(Items.AMETHYST_CLUSTER, MEDIUM_QUARTZITE_BUD);
            insertAfter(Items.AMETHYST_CLUSTER, SMALL_QUARTZITE_BUD);
            insertAfter(Items.AMETHYST_CLUSTER, BUDDING_QUARTZITE);
            insertAfter(Items.AMETHYST_CLUSTER, QUARTZITE);

            insertAfter(Items.HAY_BLOCK, TROPICAL_FISH_BARREL);
            insertAfter(Items.HAY_BLOCK, PUFFERFISH_BARREL);
            insertAfter(Items.HAY_BLOCK, BILLHOOK_BASS_BARREL);
            insertAfter(Items.HAY_BLOCK, SALMON_BARREL);
            insertAfter(Items.HAY_BLOCK, COD_BARREL);
            insertAfter(Items.HAY_BLOCK, PEAR_CRATE);
            insertAfter(Items.HAY_BLOCK, APPLE_CRATE);

//            event.accept(REMAINS);

            insertAfter(Items.SPRUCE_LOG, PINE.log());
            insertAfter(Items.SPRUCE_LEAVES, PINE_LEAVES);
            insertAfter(Items.SPRUCE_SAPLING, PINE_SAPLING);

            insertBefore(Items.DARK_OAK_LOG, MAPLE.log());
            insertBefore(Items.DARK_OAK_LEAVES, MAPLE_LEAVES);
            insertBefore(Items.DARK_OAK_SAPLING, MAPLE_SAPLING);
            insertBefore(Items.DARK_OAK_LEAVES, RED_MAPLE_LEAVES);
            insertBefore(Items.DARK_OAK_SAPLING, RED_MAPLE_SAPLING);

            insertAfter(Items.DARK_OAK_LOG, WALNUT.log());
            insertAfter(Items.DARK_OAK_LEAVES, WALNUT_LEAVES);
            insertAfter(Items.DARK_OAK_SAPLING, WALNUT_SAPLING);

            insertBefore(Items.MANGROVE_LOG, WILLOW.log());
            insertBefore(Items.MANGROVE_LEAVES, WILLOW_LEAVES);
            insertBefore(Items.MANGROVE_PROPAGULE, WILLOW_SAPLING);

        }

        if (tab == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            insertAfter(Items.BOOKSHELF, WARPED_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, CRIMSON_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, CHERRY_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, BAMBOO_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, MANGROVE_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, WILLOW.bookshelf());
            insertAfter(Items.BOOKSHELF, WALNUT.bookshelf());
            insertAfter(Items.BOOKSHELF, DARK_OAK_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, MAPLE.bookshelf());
            insertAfter(Items.BOOKSHELF, ACACIA_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, JUNGLE_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, BIRCH_BOOKSHELF);
            insertAfter(Items.BOOKSHELF, PINE.bookshelf());
            insertAfter(Items.BOOKSHELF, SPRUCE_BOOKSHELF);
            insertAfter(Items.SPRUCE_HANGING_SIGN, PINE_HANGING_SIGN);
            insertAfter(Items.SPRUCE_HANGING_SIGN, PINE_SIGN);
            insertAfter(Items.DARK_OAK_HANGING_SIGN, WALNUT_HANGING_SIGN);
            insertAfter(Items.DARK_OAK_HANGING_SIGN, WALNUT_SIGN);
            insertBefore(Items.DARK_OAK_SIGN, MAPLE_SIGN);
            insertBefore(Items.DARK_OAK_SIGN, MAPLE_HANGING_SIGN);
            insertBefore(Items.MANGROVE_SIGN, WILLOW_SIGN);
            insertBefore(Items.MANGROVE_SIGN, WILLOW_HANGING_SIGN);
            insertAfter(Items.REDSTONE_TORCH, NMLItems.SCONCE_SOUL_TORCH);
            insertAfter(Items.REDSTONE_TORCH, NMLItems.SCONCE_TORCH);
            insertAfter(Items.CAULDRON, TAP);
            insertBefore(Items.SCAFFOLDING, NMLItems.WOODEN_SCAFFOLDING);
            insertAfter(Items.CONDUIT, WARDING_EFFIGY);
        }

        if (tab == CreativeModeTabs.FOOD_AND_DRINKS) {
            insertAfter(Items.COOKED_BEEF, HORSE_STEAK);
            insertAfter(Items.COOKED_BEEF, RAW_HORSE);
            insertAfter(Items.COOKED_MUTTON, COOKED_VENISON);
            insertAfter(Items.COOKED_MUTTON, RAW_VENISON);
            insertAfter(Items.COOKED_RABBIT, COOKED_FROG_LEG);
            insertAfter(Items.COOKED_RABBIT, FROG_LEG);
            insertAfter(Items.HONEY_BOTTLE, MAPLE_SYRUP_BOTTLE);
            insertAfter(Items.PUMPKIN_PIE, PANCAKE);
            if (Mods.FARMERSDELIGHT.isLoaded()) {
                insertAfter(Items.PUMPKIN_PIE, FDIntegration.PEAR_COBBLER_ITEM);
                insertAfter(Items.CAKE, FDIntegration.FRUIT_CAKE);
                insertBefore(Items.MILK_BUCKET, FDIntegration.PESTO_BOTTLE);
            }
            insertAfter(Items.COOKIE, MAPLE_TART);
            insertAfter(Items.COOKIE, SWEET_TART);
            insertAfter(Items.ENCHANTED_GOLDEN_APPLE, SYRUPED_PEAR);
            insertAfter(Items.ENCHANTED_GOLDEN_APPLE, PEAR);
            insertAfter(Items.APPLE, HONEYED_APPLE);
            insertAfter(Items.MELON_SLICE, TRAIL_MIX);
            insertAfter(Items.MELON_SLICE, WALNUTS);
            insertAfter(Items.MELON_SLICE, PINE_NUTS);
            insertAfter(Items.MUSHROOM_STEW, MASHED_POTATOES_WITH_MUSHROOMS);
            insertAfter(Items.POISONOUS_POTATO, GRILLED_MUSHROOMS);
            insertAfter(Items.COOKED_SALMON, COOKED_BILLHOOK_BASS);
            insertAfter(Items.COOKED_SALMON, BILLHOOK_BASS);
            insertAfter(Items.SPIDER_EYE, HARDTACK);
//            insertAfter(Items.TROPICAL_FISH, CAVE_CARP);
        }

        if (tab == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            insertAfter(Items.SPRUCE_CHEST_BOAT, PINE_CHEST_BOAT);
            insertAfter(Items.SPRUCE_CHEST_BOAT, PINE_BOAT);
            insertBefore(Items.DARK_OAK_BOAT, MAPLE_BOAT);
            insertBefore(Items.DARK_OAK_BOAT, MAPLE_CHEST_BOAT);
            insertBefore(Items.MANGROVE_BOAT, WILLOW_BOAT);
            insertBefore(Items.MANGROVE_BOAT, WILLOW_CHEST_BOAT);
            insertAfter(Items.DARK_OAK_CHEST_BOAT, WALNUT_CHEST_BOAT);
            insertAfter(Items.DARK_OAK_CHEST_BOAT, WALNUT_BOAT);
            insertAfter(Items.SALMON_BUCKET, BILLHOOK_BASS_BUCKET);
            insertAfter(Items.ELYTRA, WARDING_EFFIGY);
//            insertAfter(Items.TROPICAL_FISH_BUCKET, CAVE_CARP_BUCKET);
            if (!event.getFlags().contains(FeatureFlags.BUNDLE)) event.insertBefore(Items.FLINT_AND_STEEL.getDefaultInstance(), Items.BUNDLE.getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        if (tab == CreativeModeTabs.COMBAT) {
            insertAfter(Items.WIND_CHARGE, FIREBOMB);
            insertBefore(Items.TNT, EXPLOSIVE);
            insertAfter(Items.EGG, RESIN_OIL_BOTTLE);
        }

        if (tab == CreativeModeTabs.INGREDIENTS) {
            insertAfter(Items.HONEYCOMB, RESIN);
            insertAfter(RESIN.asItem(), RESIN_OIL_BOTTLE);
        }

        if (tab == CreativeModeTabs.REDSTONE_BLOCKS) {
            insertAfter(Blocks.LIGHTNING_ROD.asItem(), SPIKE_TRAP);
        }

        if (tab == CreativeModeTabs.SPAWN_EGGS) {
            insertAfter(Blocks.SPAWNER.asItem(), MONSTER_ANCHOR);
//            insertAfter(Items.CREEPER_SPAWN_EGG, MOOSE_SPAWN_EGG);
            insertAfter(Items.CREEPER_SPAWN_EGG, DEER_SPAWN_EGG);
            insertAfter(Items.BEE_SPAWN_EGG, BILLHOOK_BASS_SPAWN_EGG);
//            insertAfter(Items.CHICKEN_SPAWN_EGG, GOOSE_SPAWN_EGG);
        }

        if (Mods.CREATE.isLoaded())
            event.remove(Mods.CREATE.getItem("honeyed_apple").getDefaultInstance(), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }
}
