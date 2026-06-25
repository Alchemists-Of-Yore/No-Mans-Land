package com.farcr.nomansland.common.registry.blocks;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;

public class NMLFlammables {
    public static void register() {
        FireBlock fireBlock = (FireBlock) Blocks.FIRE;

        fireBlock.setFlammable(NMLBlocks.YELLOW_BIRCH_LEAVES.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.AUTUMNAL_OAK_LEAVES.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.PALE_CHERRY_LEAVES.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.FROSTED_LEAVES.get(), 30, 60);

        fireBlock.setFlammable(NMLBlocks.APPLE_FRUIT_LEAVES.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.PEAR_FRUIT_LEAVES.get(), 30, 60);

        fireBlock.setFlammable(NMLBlocks.GRASS_SPROUTS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.OAT_GRASS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.SHORT_BEACHGRASS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.TALL_BEACHGRASS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.FIDDLEHEAD.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.DRIED_GRASS.get(), 45, 60);
        fireBlock.setFlammable(NMLBlocks.FROSTED_GRASS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.BEARD_MOSS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.MYCELIUM_GROWTHS.get(), 10, 30);
        fireBlock.setFlammable(NMLBlocks.MYCELIUM_SPROUTS.get(), 10, 30);
        fireBlock.setFlammable(NMLBlocks.CATTAIL.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.REEDS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.CAVE_WEEDS.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.SHELF_MUSHROOM_BLOCK.get(), 30, 60);

        NMLBlocks.MAPLE.setFlammables();
        fireBlock.setFlammable(NMLBlocks.MAPLE_LEAVES.get(), 30, 60);
        fireBlock.setFlammable(NMLBlocks.RED_MAPLE_LEAVES.get(), 30, 60);

        NMLBlocks.PINE.setFlammables();
        fireBlock.setFlammable(NMLBlocks.PINE_LEAVES.get(), 30, 60);

        NMLBlocks.WALNUT.setFlammables();
        fireBlock.setFlammable(NMLBlocks.WALNUT_LEAVES.get(), 30, 60);

        NMLBlocks.WILLOW.setFlammables();
        fireBlock.setFlammable(NMLBlocks.WILLOW_LEAVES.get(), 30, 60);

        fireBlock.setFlammable(NMLBlocks.TRIMMED_OAK_PLANKS.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.SPRUCE_BOOKSHELF.get(), 30, 20);
        fireBlock.setFlammable(NMLBlocks.TRIMMED_SPRUCE_PLANKS.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.BIRCH_BOOKSHELF.get(), 30, 20);
        fireBlock.setFlammable(NMLBlocks.TRIMMED_BIRCH_PLANKS.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.JUNGLE_BOOKSHELF.get(), 30, 20);
        fireBlock.setFlammable(NMLBlocks.TRIMMED_JUNGLE_PLANKS.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.DARK_OAK_BOOKSHELF.get(), 30, 20);
        fireBlock.setFlammable(NMLBlocks.TRIMMED_DARK_OAK_PLANKS.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.CHERRY_BOOKSHELF.get(), 30, 20);
        fireBlock.setFlammable(NMLBlocks.TRIMMED_CHERRY_PLANKS.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.MANGROVE_BOOKSHELF.get(), 30, 20);
        fireBlock.setFlammable(NMLBlocks.TRIMMED_MANGROVE_PLANKS.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.BAMBOO_BOOKSHELF.get(), 30, 20);
        fireBlock.setFlammable(NMLBlocks.TRIMMED_BAMBOO_PLANKS.get(), 5, 20);


        fireBlock.setFlammable(NMLBlocks.SALMON_BARREL.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.COD_BARREL.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.PUFFERFISH_BARREL.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.BILLHOOK_BASS_BARREL.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.TROPICAL_FISH_BARREL.get(), 5, 20);
//        fireBlock.setFlammable(NMLBlocks.CAVE_CARP_BARREL.get(), 5, 20);

        fireBlock.setFlammable(NMLBlocks.APPLE_CRATE.get(), 5, 20);
        fireBlock.setFlammable(NMLBlocks.PEAR_CRATE.get(), 5, 20);

        fireBlock.setFlammable(NMLBlocks.OIL_PUDDLE.get(), 50, 75);
    }


}
