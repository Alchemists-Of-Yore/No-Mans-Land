package com.farcr.nomansland.datagen.loot;

import net.minecraft.world.level.block.Block;

public class CandleCakeBlockLootType extends BlockLootType {
    private final Block candle;

    public CandleCakeBlockLootType(Block candle) {
        this.candle = candle;
    }

    public Block getCandle() {
        return candle;
    }
}
