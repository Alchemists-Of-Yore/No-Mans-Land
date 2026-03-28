package com.farcr.nomansland.client.renderer.effect;

import com.mojang.blaze3d.pipeline.TextureTarget;

public class RenderTargetUnclear extends TextureTarget {
    public RenderTargetUnclear(int width, int height, boolean useDepth, boolean clearError) {
        super(width, height, useDepth, clearError);
    }

    @Override public void clear(boolean clearError) {}
}
