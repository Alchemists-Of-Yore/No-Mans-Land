package com.farcr.nomansland.common.friend;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;

public record BuddyStar(
    BuddyStarColor color,
    float hueOffset,
    float saturationOffset,
    float lightnessOffset,
    long seed
) {
    public static final Codec<BuddyStar> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BuddyStarColor.CODEC.fieldOf("color").forGetter(BuddyStar::color),
            Codec.FLOAT.fieldOf("hue_offset").forGetter(BuddyStar::hueOffset),
            Codec.FLOAT.fieldOf("saturation_offset").forGetter(BuddyStar::saturationOffset),
            Codec.FLOAT.fieldOf("lightness_offset").forGetter(BuddyStar::lightnessOffset),
            Codec.LONG.fieldOf("seed").forGetter(BuddyStar::seed)
        ).apply(instance, BuddyStar::new)
    );

    public static final int FIRST_RING_SIZE = 6;
    public static final float FIRST_RING_DISTANCE = 8f;
    public static final float RING_DISTANCE_STEP = 5f;
    public static final float MAX_RANDOMNESS = 0.3f;

    private static int ringSize(int ring) {
        float circumference = (float) (2 * Math.PI * (FIRST_RING_DISTANCE + ring * RING_DISTANCE_STEP));
        float firstCircumference = (float) (2 * Math.PI * FIRST_RING_DISTANCE);
        return Math.max(FIRST_RING_SIZE, Math.round(FIRST_RING_SIZE * (circumference / firstCircumference)));
    }

    private static float ringRandomness(int ring) {
        if (ring == 0) return 0f;
        return Math.min(ring * 0.1f, MAX_RANDOMNESS);
    }

    public static BuddyStar fromVariant(String variantName, RandomSource random) {
        BuddyStarColor color = BuddyStarColor.fromVariantName(variantName);

        float variation = 0.05f;
        float hOff = (random.nextFloat() * 2 - 1) * variation;
        float sOff = (random.nextFloat() * 2 - 1) * variation;
        float lOff = (random.nextFloat() * 2 - 1) * variation;

        return new BuddyStar(color, hOff, sOff, lOff, random.nextLong());
    }

    public float[] getPosition(int index) {
        int ring = 0;
        int ringStart = 0;
        int currentRingSize = ringSize(0);
        while (index >= ringStart + currentRingSize) {
            ringStart += currentRingSize;
            ring++;
            currentRingSize = ringSize(ring);
        }
        int indexInRing = index - ringStart;

        float spacing = 360f / currentRingSize;
        float angle = spacing * indexInRing;
        float distance = FIRST_RING_DISTANCE + ring * RING_DISTANCE_STEP;

        float randomness = ringRandomness(ring);
        RandomSource jitterRandom = RandomSource.create(seed);
        angle += (jitterRandom.nextFloat() * 2 - 1) * spacing * randomness;
        distance += (jitterRandom.nextFloat() * 2 - 1) * RING_DISTANCE_STEP * randomness;

        return new float[]{angle, distance};
    }

    public float getFlickerAlpha(long timeMs) {
        double t = timeMs / 1000.0;
        double v = Math.sin(t * 0.7 + seed)
            + Math.sin(t * 1.3 + seed * 0.7)
            + Math.sin(t * 2.1 + seed * 1.3);
        v /= 3.0;
        float normalized = (float) (v * 0.5 + 0.5);
        return 0.6f + normalized * 0.4f;
    }

    public float[] getRgb() {
        float h = color.hue() + hueOffset;
        float s = clamp(color.saturation() + saturationOffset);
        float l = clamp(color.lightness() + lightnessOffset);

        if (h < 0) h += 1f;
        if (h > 1) h -= 1f;

        return hslToRgb(h, s, l);
    }

    public static float[] hslToRgb(float h, float s, float l) {
        if (s == 0)
            return new float[]{l, l, l};

        float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        float p = 2 * l - q;

        return new float[]{
            hueToRgb(p, q, h + 1f / 3f),
            hueToRgb(p, q, h),
            hueToRgb(p, q, h - 1f / 3f)
        };
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1f;
        if (t > 1) t -= 1f;
        if (t < 1f / 6f) return p + (q - p) * 6f * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
