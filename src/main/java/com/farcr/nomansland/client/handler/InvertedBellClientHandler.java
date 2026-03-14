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

/**
 * Runs clientside logic of inverted bells visually swinging, postprocessing shaders, overriding mouse input, and changing audio volume
 */
public class InvertedBellClientHandler {
    // bell swinging when interacted with
    private static final int ANIMATION_DURATION = 100; // ticks to wobble for
    private static final double ANIMATION_INTENSITY = 0.25; // scalar for maximum angle of the wobble
    private static final double ANIMATION_SPEED = 0.25; // frequency of the wobble. higher = faster

    // shader / look slow
    public static final int FADE_IN_TIME = InvertedBellServerHandler.TELEPORT_PLAYER_TIME; // 35
    public static final int FADE_OUT_TIME = 20;
    public static final int FADE_OUT_PAINFUL_TIME = InvertedBellServerHandler.FAILURE_NAUSEA_DURATION / 2; // 100

    private int fadeTimer;
    private State state = State.INACTIVE;

    public static InvertedBellClientHandler instance = new InvertedBellClientHandler();
    public PostChain postChain = null;

    private float intensity = 0;
    private float previousIntensity = 0;

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



    public State getState() {
        return this.state;
    }

    public void startFadeIn() {
        this.fadeTimer = 1;
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
        this.fadeTimer = 1;
        this.state = State.FADE_OUT;
    }

    public void startFadeOutPainful() {
        this.fadeTimer = 1;
        this.state = State.FADE_OUT_PAINFUL;
        Minecraft.getInstance().player.displayClientMessage(Component.translatable("block.nomansland.inverted_bell.bad_teleport"), true);
    }

    public void stop() {
        this.fadeTimer = 0;
        this.state = State.INACTIVE;
    }

    public void tick() {
        if (animationTimer > 0) {
            animationTimer--;
        }
        switch (this.state) {
            case FADE_IN -> {
                this.fadeTimer++;
                // safeguard if server lags (badly) (which it probably will)
                if (this.fadeTimer >= FADE_IN_TIME + 100) {
                    this.startFadeOut();
                }
            }
            case FADE_OUT -> {
                this.fadeTimer++;
                if (this.fadeTimer >= FADE_OUT_TIME) {
                    this.stop();
                }
            }
            case FADE_OUT_PAINFUL -> {
                this.fadeTimer++;
                if (this.fadeTimer >= FADE_OUT_PAINFUL_TIME) {
                    this.stop();
                }
            }
        }
        this.previousIntensity = this.intensity;
        this.intensity = (float) Mth.lerp(0.5, this.intensity, this.state.getIntensity(this.fadeTimer));
        if (this.intensity < 1E-4) {
            this.intensity = 0;
        }
    }

    public boolean isActive() {
        return this.fadeTimer > 0;
    }

    public float getIntensity(float pt) {
        return Mth.lerp(pt, this.previousIntensity, this.intensity);
    }

    public void render(Minecraft minecraft, float pt) {
        float intensity = this.getIntensity(pt);
        if (this.postChain != null && intensity > 0) {
            this.postChain.resize(minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());
            this.postChain.setUniform("Fade", intensity);
            this.postChain.process(pt);
        }
    }

    public void onHit(int direction) {
        InvertedBellClientHandler.direction = direction;
        animationTimer = ANIMATION_DURATION;
    }

    private static float getBellAnimationAngle(float pt) {
        float t = animationTimer - pt;
        if (animationTimer > 0) {
            double decay = (Math.exp(t / ANIMATION_DURATION) - 1);
            double wobble = Math.sin((ANIMATION_DURATION - t) * ANIMATION_SPEED) * ANIMATION_INTENSITY;
            return (float) (decay * wobble);
        }
        return 0;
    }

    public Quaternionf getBellAnimationRotation(float pt) {
        return Axis.XN.rotation(InvertedBellClientHandler.getBellAnimationAngle(pt) * InvertedBellClientHandler.direction);
    }

    public enum State {
        INACTIVE(0, false),
        FADE_IN(FADE_IN_TIME, true),
        FADE_OUT(FADE_OUT_TIME, false),
        FADE_OUT_PAINFUL(FADE_OUT_PAINFUL_TIME, false);

        private final int duration;
        private final boolean inDir;

        State(int duration, boolean inDir) {
            this.duration = duration;
            this.inDir = inDir;
        }

        public float getIntensity(float timer) {
            if (this.duration == 0) {
                return 0;
            }

            if (this.inDir) {
                return Mth.clamp(timer / this.duration, 0, 1);
            } else {
                return 1 - Mth.clamp(timer / this.duration, 0, 1);
            }
        }
    }
}
