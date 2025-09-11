package com.farcr.nomansland.client.model.tortoise;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class TortoiseAnimation {
    public static final AnimationDefinition BABY_TRANSFORM = AnimationDefinition.Builder.withLength(0)
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0, KeyframeAnimations.scaleVec(1.4F, 1.4F, 1.4F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition TORTOISE_WALK = AnimationDefinition.Builder.withLength(2).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, -2.43F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0, -2.6F, 0.31F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0, -2.6F, 2.74F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(0, 0, 2.43F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(0, 2.6F, -0.31F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.degreeVec(0, 2.6F, -2.74F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.degreeVec(0, 0, -2.43F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0.24F, 0.3F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 0.4F, 0.15F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.posVec(0, 0.16F, -0.15F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.posVec(0, -0.24F, -0.3F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.posVec(0, -0.4F, -0.15F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.posVec(0, -0.16F, 0.15F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.posVec(0, 0.24F, 0.3F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(-3.71F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-11.74F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-8.03F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(3.71F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(11.74F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.degreeVec(8.03F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.degreeVec(-3.71F, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.0417F, KeyframeAnimations.posVec(0, 0, 2), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.75F, KeyframeAnimations.posVec(0, 1.5F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(3.71F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(11.74F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(8.03F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(-3.71F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-11.74F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.degreeVec(-8.03F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.degreeVec(3.71F, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 2), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.posVec(0, 1.5F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.posVec(0, 0, 2), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("right_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(-12, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(12, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.degreeVec(-6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.degreeVec(-12, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("right_hind_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 2), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.25F, KeyframeAnimations.posVec(0, 1.5F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("left_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(12, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(-12, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.3333F, KeyframeAnimations.degreeVec(-6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.6667F, KeyframeAnimations.degreeVec(6, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.degreeVec(12, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("left_hind_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 2), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.posVec(0, 1.5F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(2, KeyframeAnimations.posVec(0, 0, 2), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();

    public static final AnimationDefinition TORTOISE_HIDE = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0, -5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.375F, KeyframeAnimations.degreeVec(0, 5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0, -5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.4583F, KeyframeAnimations.degreeVec(0, 5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, -2, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(0, -4, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, -4, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(32.5F, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(32.5F, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 0, 16), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 16), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0, KeyframeAnimations.scaleVec(1, 1, 1), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.scaleVec(1, 1, 1), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.scaleVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.scaleVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(8, 2, 5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(8, 2, 5), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(-8, 2, 5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(-8, 2, 5), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(25, -45, 67), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(25, -45, 67), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_hind_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(8, 2, -5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(8, 2, -5), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2917F, KeyframeAnimations.degreeVec(25, 45, -67), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(25, 45, -67), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_hind_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(-8, 2, -5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(-8, 2, -5), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition TORTOISE_EMERGE = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0417F, KeyframeAnimations.degreeVec(0, 5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.0833F, KeyframeAnimations.degreeVec(0, -5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.125F, KeyframeAnimations.degreeVec(0, 5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0, -5, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, -4, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(0, -4, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, -2, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(32.5F, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(32.5F, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 16), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(0, 0, 16), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0, KeyframeAnimations.scaleVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.scaleVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.scaleVec(1, 1, 1), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.scaleVec(1, 1, 1), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(-8, 2, 5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(-8, 2, 5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-75, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(8, 2, 5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(8, 2, 5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(25, -45, 67), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(25, -45, 67), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("right_hind_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(8, 2, -5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(8, 2, -5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_hind_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(25, 45, -67), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.2083F, KeyframeAnimations.degreeVec(25, 45, -67), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .addAnimation("left_hind_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(-8, 2, -5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.1667F, KeyframeAnimations.posVec(-8, 2, -5), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.25F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM),
                    new Keyframe(0.5F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.CATMULLROM)
            ))
            .build();

    public static final AnimationDefinition TORTOISE_LAY_EGG = AnimationDefinition.Builder.withLength(1).looping()
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 3.5F, -0.4F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.0833F, KeyframeAnimations.degreeVec(0, 1.75F, 0.05F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0, -1.75F, 0.49F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.25F, KeyframeAnimations.degreeVec(0, -3.5F, 0.44F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(0, -1.75F, -0.06F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(0, 1.75F, -0.53F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0, 3.5F, -0.49F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(0, 1.75F, 0.06F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(0, -1.75F, 0.58F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.degreeVec(0, -3.5F, 0.53F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(0, -1.75F, -0.07F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(0, 1.75F, -0.62F), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(0, 3.5F, -0.57F), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(12.5F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.0833F, KeyframeAnimations.degreeVec(12.5F, 4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(12.5F, 4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(12.5F, -4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.4167F, KeyframeAnimations.degreeVec(12.5F, -4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.5833F, KeyframeAnimations.degreeVec(12.5F, 4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(12.5F, 4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(12.5F, -4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.9167F, KeyframeAnimations.degreeVec(12.5F, -4.33F, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(12.5F, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("left_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(-1, 0, 1), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.625F, KeyframeAnimations.posVec(0, 1, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.75F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.degreeVec(-17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.6667F, KeyframeAnimations.degreeVec(17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.8333F, KeyframeAnimations.degreeVec(17.32F, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.degreeVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .addAnimation("right_front_leg", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0, KeyframeAnimations.posVec(0, 1, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.125F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.3333F, KeyframeAnimations.posVec(0, 0, 0), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(0.625F, KeyframeAnimations.posVec(1, 0, 1), AnimationChannel.Interpolations.LINEAR),
                    new Keyframe(1, KeyframeAnimations.posVec(0, 1, 0), AnimationChannel.Interpolations.LINEAR)
            ))
            .build();
}
