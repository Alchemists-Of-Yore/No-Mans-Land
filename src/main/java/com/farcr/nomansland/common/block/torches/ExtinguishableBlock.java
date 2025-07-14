package com.farcr.nomansland.common.block.torches;

import net.minecraft.world.level.block.Block;

public record ExtinguishableBlock(Block litBlock, Block extinguishedBlock) {
}
