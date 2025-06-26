package com.farcr.nomansland.datagen.loot;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class CandleCakeBlockLootType extends BlockLootType {
    private final Block candle;

    public CandleCakeBlockLootType(Block candle) {
        this.candle = candle;
    }

    public Block getCandle() {
        return candle;
    }
}
