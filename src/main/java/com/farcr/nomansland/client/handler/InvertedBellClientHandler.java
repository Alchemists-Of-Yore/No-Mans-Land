package com.farcr.nomansland.client.handler;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.io.IOException;

public class InvertedBellClientHandler {
    public static InvertedBellClientHandler instance = new InvertedBellClientHandler();
    public PostChain postChain = null;

    private float fade = 0;
    private float previousFade = 0;

    // mildly evil but there will only ever be one on screen :3
    public static int animationTimer = 0;
    public static Direction direction;

    public InvertedBellClientHandler() {
        try {
            // todo resource pack reloadable maybe
            this.postChain = new PostChain(
                    Minecraft.getInstance().getTextureManager(),
                    Minecraft.getInstance().getResourceManager(),
                    Minecraft.getInstance().getMainRenderTarget(),
                    NoMansLand.location("shaders/post/inverted_bell.json")
            );
        } catch (IOException e) {
            NoMansLand.LOGGER.error("Failed to load shader shaders/post/inverted_bell.json", e);
        }
    }

    public static final int FADE_IN_TIME = InvertedBellServerHandler.TELEPORT_WINDUP_TIME;
    public static final int FADE_OUT_TIME = 20;
    public static final int FADE_OUT_PAINFUL_TIME = InvertedBellServerHandler.FAILURE_NAUSEA_DURATION;

    private int timer;
    private State state = State.INACTIVE;

    public void startFadeIn() {
        this.timer = 1;
        this.state = State.FADE_IN;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("fading in"), true);
    }

    public void startFadeOut() {
        this.timer = 1;
        this.state = State.FADE_OUT;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("fading out"), true);
    }

    public void startFadeOutPainful() {
        this.timer = 1;
        this.state = State.FADE_OUT_PAINFUL;
        Minecraft.getInstance().player.displayClientMessage(Component.literal("oof outch owie :("), true);
    }

    public void stop() {
        this.timer = 0;
        this.state = State.INACTIVE;
    }

    public void tick() {
        if (animationTimer > 0) {
            animationTimer--;
        }
        switch (state) {
            case FADE_IN -> {
                this.timer++;
                // safeguard if packet explodes or something
                if (this.timer >= FADE_IN_TIME + 20) {
                    this.startFadeOut();
                }
            }
            case FADE_OUT -> {
                this.timer++;
                if (this.timer >= FADE_OUT_TIME) {
                    this.stop();
                }
            }
            case FADE_OUT_PAINFUL -> {
                this.timer++;
                if (this.timer >= FADE_OUT_PAINFUL_TIME) {
                    this.stop();
                }
            }
            default -> {}
        }
        this.previousFade = this.fade;
        this.fade = (float) Mth.lerp(0.5, this.fade, this.getTargetFade());
    }

    public boolean isActive() {
        return this.timer > 0;
    }

    private float getTargetFade() {
        return switch (this.state) {
            case FADE_IN -> (float) this.timer / FADE_IN_TIME;
            case FADE_OUT -> 1 - (float) this.timer / FADE_OUT_TIME;
            case FADE_OUT_PAINFUL -> 1 - (float) this.timer / FADE_OUT_PAINFUL_TIME;
            default -> 0;
        };
    }

    public float getFade(float pt) {
        return Mth.lerp(pt, this.previousFade, this.fade);
    }

    public void render(Minecraft minecraft, float pt) {
        float fade = this.getFade(pt);
        if (this.postChain != null && fade > 0) {
            this.postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            this.postChain.setUniform("Fade", fade);
            this.postChain.process(pt);
        }
    }

    public void onHit(Direction direction) {
        InvertedBellClientHandler.direction = direction;
        animationTimer = ANIMATION_DURATION;
    }

    private static final int ANIMATION_DURATION = 100;
    private static final double ANIMATION_DECAY = 0.7;
    private static final double ANIMATION_INTENSITY = 0.25;
    private static final double ANIMATION_SPEED = 0.25;
    private float getAnimationAngle(float pt) {
        float t = animationTimer - pt;
        if (animationTimer > 0) {
            double decay = (Math.exp(t / ANIMATION_DURATION) - 1) / (Math.exp(ANIMATION_DECAY) - 1);
            double wobble = Math.sin((ANIMATION_DURATION - t) * ANIMATION_SPEED) * ANIMATION_INTENSITY;
            return (float) (decay * wobble);
        }
        return 0;
    }

    public @Nullable Quaternionf getAnimationRotation(float pt) {
        if (this.direction != null) {
            return (switch (this.direction) {
                case NORTH -> Axis.XN;
                case SOUTH -> Axis.XP;
                case EAST -> Axis.ZN;
                case WEST -> Axis.ZP;
                default -> throw new IllegalStateException("Unexpected value: " + this.direction);
            }).rotation(this.getAnimationAngle(pt));
        }
        return null;
    }

    public enum State {
        INACTIVE,
        FADE_IN,
        FADE_OUT,
        FADE_OUT_PAINFUL
    }
}
