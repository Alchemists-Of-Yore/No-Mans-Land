package com.farcr.nomansland.client.renderer.effect;

import com.farcr.nomansland.NoMansLand;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;

public class AccumulateZoomRenderer {
    public static AccumulateZoomRenderer INSTANCE = new AccumulateZoomRenderer();
    public static AccumulateZoomRenderer getInstance() { return INSTANCE; }

    public static ResourceLocation ACCUMULATE_ZOOM_SHADER = NoMansLand.location("shaders/post/accumulate_zoom.json");
    public PostChain postChain;
    public void setupPostChain() throws IOException {
        Minecraft minecraft = Minecraft.getInstance();
        PostChain postChain = new PostChain(
            minecraft.getTextureManager(), minecraft.getResourceManager(),
            minecraft.getMainRenderTarget(), AccumulateZoomRenderer.ACCUMULATE_ZOOM_SHADER
        );
        RenderTarget swapTarget = postChain.getTempTarget("swap");
        PostPass pass = postChain.addPass("nomansland:accumulate_zoom", swapTarget, persistentTarget, false);
        pass.getEffect().setSampler("DiffuseSampler", swapTarget::getColorTextureId);
        pass.getEffect().setSampler("PreviousSampler", persistentTarget::getColorTextureId);

        postChain.addPass("blit", persistentTarget, minecraft.getMainRenderTarget(), false);
        postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
        this.postChain = postChain;
    }

    private final RenderTargetUnclear persistentTarget = new RenderTargetUnclear(100, 100, false, false);

    public float zoomOut = 0.0f;
    public float fadeOut = 0.0f;

    public void render(Minecraft minecraft, float partialTicks) {
        if (postChain != null && (zoomOut > 0.0f || fadeOut > 0.0f) && !minecraft.isPaused()) {
            if (persistentTarget.width != minecraft.getWindow().getWidth()
            || persistentTarget.height != minecraft.getWindow().getHeight()) {
                persistentTarget.resize(
                    minecraft.getWindow().getWidth(),
                    minecraft.getWindow().getHeight(),
                    false
                );
            }
            postChain.setUniform("zoomOut", zoomOut);
            postChain.setUniform("fadeOut", Math.max(fadeOut, 0.0f));
            postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            postChain.process(partialTicks);
        }
    }
}
