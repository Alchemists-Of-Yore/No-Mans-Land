package com.farcr.nomansland.common.block.torches;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A pair of lit and extinguished blocks
 *
 * @param litBlock
 * @param extinguishedBlock
 */
public record ExtinguishableBlockPairing(Block litBlock, Block extinguishedBlock) {

    /**
     * @return Whether the given block is the lit version of this pairing.
     */
    public boolean isLitVersion(final Block toCheck) {
        return toCheck.equals(this.litBlock);
    }

    /**
     * @return Whether the given block is the lit version of this pairing.
     */
    public boolean isLitVersion(final BlockState toCheck) {
        return toCheck.is(this.litBlock);
    }

    /**
     * @return Whether the given block is the extinguished version of this pairing.
     */
    public boolean isExtinguishedVersion(final Block toCheck) {
        return toCheck.equals(this.extinguishedBlock);
    }

    /**
     * @return Whether the given block is the extinguished version of this pairing.
     */
    public boolean isExtinguishedVersion(final BlockState toCheck) {
        return toCheck.is(this.extinguishedBlock);
    }

}
