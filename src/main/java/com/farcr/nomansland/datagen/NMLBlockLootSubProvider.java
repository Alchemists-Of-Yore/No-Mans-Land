package com.farcr.nomansland.datagen;

import com.farcr.nomansland.common.definitions.BlockDefinition;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.datagen.loot.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.HashSet;

public class NMLBlockLootSubProvider extends BlockLootSubProvider {
    private final HolderLookup.Provider provider;

    public NMLBlockLootSubProvider(HolderLookup.Provider provider) {
        super(new HashSet<>(), FeatureFlags.REGISTRY.allFlags(), provider);
        this.provider = provider;
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
//        return NMLBlocks.BLOCKS.getEntries().stream().map(e -> (Block)e.value()).toList();

        return NMLBlocks.BLOCK_DEFINITIONS.stream()
                .filter(blockDefinition -> !(blockDefinition.lootType() instanceof CustomBlockLootType))
                .map(BlockDefinition::block)
                .toList();
    }

    @Override
    protected void generate() {
        for (BlockDefinition<?> definition : NMLBlocks.BLOCK_DEFINITIONS) {
            Block block = definition.get();
            BlockLootType lootType = definition.lootType();

            // TODO: make shears go to c:tools/shear
            if (lootType instanceof SelfBlockLootType)
                dropSelf(block);
            else if (lootType instanceof OtherBlockLootType otherBlockLootType)
                add(block, createSingleItemTable(otherBlockLootType.getBlock()));
            else if (lootType instanceof ShearsBlockLootType)
                add(block, createShearsOnlyDrop(block));
            else if (lootType instanceof OtherShearsBlockLootType otherShearsBlockLootType)
                add(block, createShearsOnlyDrop(otherShearsBlockLootType.getBlock()));
            else if (lootType instanceof SlabBlockLootType)
                add(block, createSlabItemTable(block));
            else if (lootType instanceof DoorBlockLootType)
                add(block, createDoorTable(block));
            else if (lootType instanceof CandleCakeBlockLootType candleCakeBlockLootType)
                add(block, createCandleCakeDrops(candleCakeBlockLootType.getCandle()));
            else if (lootType instanceof FlowerPotBlockLootType flowerPotBlockLootType)
                add(block, createPotFlowerItemTable(flowerPotBlockLootType.getPlant()));
            else if (lootType instanceof BookshelfBlockLootType)
                add(block, createSelfDropDispatchTable(block, hasSilkTouch(), this.applyExplosionDecay(block, LootItem.lootTableItem(Items.BOOK).apply(SetItemCountFunction.setCount(ConstantValue.exactly(3.0f))))));
        }
    }
}
