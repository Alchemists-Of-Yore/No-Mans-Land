package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.client.renderer.item.PotItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class PotClientItemExtensions implements IClientItemExtensions {
    private PotItemRenderer renderer;

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        renderer = renderer == null ? new PotItemRenderer() : renderer;
        return renderer;
    }
}
