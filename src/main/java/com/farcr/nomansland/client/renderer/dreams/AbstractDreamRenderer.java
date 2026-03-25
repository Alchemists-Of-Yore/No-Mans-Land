package com.farcr.nomansland.client.renderer.dreams;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;

public abstract class AbstractDreamRenderer implements AutoCloseable {
    public abstract boolean render(LevelRenderer levelRenderer,
                                   PoseStack poseStack,
                                   DeltaTracker deltaTracker,
                                   Matrix4f frustumMatrix,
                                   Matrix4f projectionMatrix);
}
