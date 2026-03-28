package com.farcr.nomansland.client.renderer.effect;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

public class AccumulateZoomRenderer {
    public static AccumulateZoomRenderer INSTANCE = new AccumulateZoomRenderer();
    public static AccumulateZoomRenderer getInstance() { return INSTANCE; }

    public static ResourceLocation ACCUMULATE_ZOOM_SHADER = NoMansLand.location("shaders/post/accumulate_zoom.json");
    public PostChain postChain;

    public final RenderTargetUnclear persistentTarget = new RenderTargetUnclear(100, 100, false, false);

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
