package com.farcr.nomansland.client.handler;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.extension.SoundInstanceExtension;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import java.io.IOException;

public class InvertedBellClientHandler {
    // bell swinging when interacted with
    private static final int ANIMATION_DURATION = 100;
    private static final double ANIMATION_DECAY = 0.7;
    private static final double ANIMATION_INTENSITY = 0.25;
    private static final double ANIMATION_SPEED = 0.25;

    // shader / look slow
    public static final int FADE_IN_TIME = InvertedBellServerHandler.TELEPORT_PLAYER_TIME; // 35
    public static final int FADE_OUT_TIME = 20;
    public static final int FADE_OUT_PAINFUL_TIME = InvertedBellServerHandler.FAILURE_NAUSEA_DURATION / 2; // 100

    public static InvertedBellClientHandler instance = new InvertedBellClientHandler();
    public PostChain postChain = null;

    private float fade = 0;
    private float previousFade = 0;

    // mildly evil but there will only ever be one on screen :3
    public static int animationTimer = 0;
    public static int direction;

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


    private int timer;
    private State state = State.INACTIVE;

    public State getState() {
        return this.state;
    }

    public void startFadeIn() {
        this.timer = 1;
        this.state = State.FADE_IN;
        ((LivingEntityExtension)Minecraft.getInstance().player).nml$beginBellParalysis();
        SimpleSoundInstance sound = new SimpleSoundInstance(
                SoundEvents.BELL_BLOCK.getLocation(),
                SoundSource.BLOCKS,
                1.0f,
                0.5f,
                SoundInstance.createUnseededRandom(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0,
                0.0,
                0.0,
                true
        );
        ((SoundInstanceExtension)sound).nml$setBypassDeafening(true);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public void startFadeOut() {
        this.timer = 1;
        this.state = State.FADE_OUT;
    }

    public void startFadeOutPainful() {
        this.timer = 1;
        this.state = State.FADE_OUT_PAINFUL;
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("block.nomansland.inverted_bell.bad_teleport"), true);
    }

    public void stop() {
        this.timer = 0;
        this.state = State.INACTIVE;
    }

    public void tick() {
        if (animationTimer > 0) {
            animationTimer--;
        }
        switch (this.state) {
            case FADE_IN -> {
                this.timer++;
                // safeguard if server lags (badly) (which it probably will)
                if (this.timer >= FADE_IN_TIME + 100) {
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
        if (this.fade < 1E-4) {
            this.fade = 0;
        }
    }

    public boolean isActive() {
        return this.timer > 0;
    }

    private float getTargetFade() {
        return Math.clamp(switch (this.state) {
            case FADE_IN -> (float) this.timer / FADE_IN_TIME;
            case FADE_OUT -> 1 - (float) this.timer / FADE_OUT_TIME;
            case FADE_OUT_PAINFUL -> 1 - (float) this.timer / FADE_OUT_PAINFUL_TIME;
            default -> 0;
        }, 0, 1);
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

    public void onHit(int direction) {
        InvertedBellClientHandler.direction = direction;
        animationTimer = ANIMATION_DURATION;
    }

    private static float getAnimationAngle(float pt) {
        float t = animationTimer - pt;
        if (animationTimer > 0) {
            double decay = (Math.exp(t / ANIMATION_DURATION) - 1) / (Math.exp(ANIMATION_DECAY) - 1);
            double wobble = Math.sin((ANIMATION_DURATION - t) * ANIMATION_SPEED) * ANIMATION_INTENSITY;
            return (float) (decay * wobble);
        }
        return 0;
    }

    public Quaternionf getAnimationRotation(float pt) {
        return Axis.XN.rotation(InvertedBellClientHandler.getAnimationAngle(pt) * InvertedBellClientHandler.direction);
    }

    public enum State {
        INACTIVE,
        FADE_IN,
        FADE_OUT,
        FADE_OUT_PAINFUL
    }
}
