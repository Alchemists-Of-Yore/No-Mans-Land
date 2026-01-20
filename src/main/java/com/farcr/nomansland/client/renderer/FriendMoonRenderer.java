package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.joml.Matrix4f;

public class FriendMoonRenderer {
    public enum FriendMoonAnimation {
        TALKING("talking", 1, 2),
        SURPRISED("surprised", 1, 1),
        HIDDEN("hidden", 1, 1),
        HIDDEN_2("hidden_unfocused", 1, 1),
        PHASES("phases", 6, 1);

        public static final String textureLocation = "textures/misc/friendmoon_";
        private final ResourceLocation location;

        private final int xFrames;
        private final int yFrames;
        FriendMoonAnimation(String name, int xFrames, int yFrames) {
            this.location = NoMansLand.location(textureLocation + name + ".png");

            this.xFrames = xFrames;
            this.yFrames = yFrames;
        }

        public float[] getUV(int frame) {
            int horizontalIndex = frame % xFrames;
            int verticalIndex = (frame / xFrames) % yFrames;

            return new float[] {
                horizontalIndex / (float) xFrames, (verticalIndex + 1) / (float) yFrames,
                (horizontalIndex + 1) / (float) xFrames, verticalIndex / (float) yFrames
            };
        }


        public ResourceLocation getLocation() {
            return location;
        }
    }

    public static float friendMoonOpacity = 1.0f;
    public static float getFriendMoonOpacity() {
        return friendMoonOpacity;
    }

    public static float getMoonOpacity() {
        return 1.f - getFriendMoonOpacity();
    }

    public static final float MOON_SIZE = 20f;
    public static FriendMoonAnimation MOON_ANIMATION = FriendMoonAnimation.SURPRISED;

    public static void renderFriendMoon(Tesselator tesselator, PoseStack posestack, int moonPhase) {
        RenderSystem.setShaderTexture(0, MOON_ANIMATION.getLocation());
        Matrix4f matrix4f1 = posestack.last().pose();

        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        float[] uvPositions = MOON_ANIMATION.getUV(0);
        buffer.addVertex(matrix4f1, -MOON_SIZE, -100.0F, MOON_SIZE).setUv(uvPositions[0], uvPositions[3]);
        buffer.addVertex(matrix4f1, MOON_SIZE, -100.0F, MOON_SIZE).setUv(uvPositions[2], uvPositions[3]);
        buffer.addVertex(matrix4f1, MOON_SIZE, -100.0F, -MOON_SIZE).setUv(uvPositions[2], uvPositions[1]);
        buffer.addVertex(matrix4f1, -MOON_SIZE, -100.0F, -MOON_SIZE).setUv(uvPositions[0], uvPositions[1]);

        float[] shaderColor = RenderSystem.getShaderColor();
        RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], FriendMoonRenderer.getFriendMoonOpacity());
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}
