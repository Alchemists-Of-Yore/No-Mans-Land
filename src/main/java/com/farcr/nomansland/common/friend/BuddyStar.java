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

    public static final float MIN_DISTANCE = 12f;
    public static final float MIN_RANGE = 2f;
    public static final float MAX_RANGE = 18f;
    public static final float RANGE_PER_STAR = 0.5f;

    public static BuddyStar fromVariant(String variantName, RandomSource random) {
        BuddyStarColor color = BuddyStarColor.fromVariantName(variantName);

        float variation = 0.05f;
        float hOff = (random.nextFloat() * 2 - 1) * variation;
        float sOff = (random.nextFloat() * 2 - 1) * variation;
        float lOff = (random.nextFloat() * 2 - 1) * variation;

        return new BuddyStar(color, hOff, sOff, lOff, random.nextLong());
    }

    public float getAngle() {
        RandomSource r = RandomSource.create(seed);
        return r.nextFloat() * 360f;
    }

    public float getDistance(int index) {
        RandomSource r = RandomSource.create(seed);
        r.nextFloat();
        float range = Math.min(MIN_RANGE + index * RANGE_PER_STAR, MAX_RANGE);
        return MIN_DISTANCE + r.nextFloat() * range;
    }

    public float getFlickerAlpha(long timeMs) {
        RandomSource random = RandomSource.create(seed ^ (timeMs / 80));
        float roll = random.nextFloat();
        if (roll < 0.06f)
            return 0.3f + random.nextFloat() * 0.2f;
        return 1.0f;
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
