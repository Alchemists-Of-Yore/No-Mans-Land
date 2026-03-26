package com.farcr.nomansland.common.friend;

import net.minecraft.util.StringRepresentable;

public enum BuddyStarColor implements StringRepresentable {
    RED(0xFF, 0xBE, 0xB0),
    BROWN(0xFF, 0xF4, 0xB0),
    FIELD(0xFE, 0xFF, 0xF3),
    MYCELIAL(0xF2, 0xCC, 0xF4);

    private final float hue;
    private final float saturation;
    private final float lightness;

    BuddyStarColor(int r, int g, int b) {
        float[] hsl = rgbToHsl(r / 255f, g / 255f, b / 255f);
        this.hue = hsl[0];
        this.saturation = hsl[1];
        this.lightness = hsl[2];
    }

    public float hue() { return hue; }
    public float saturation() { return saturation; }
    public float lightness() { return lightness; }

    public static final com.mojang.serialization.Codec<BuddyStarColor> CODEC = StringRepresentable.fromEnum(BuddyStarColor::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public static BuddyStarColor fromVariantName(String variantName) {
        return switch (variantName) {
            case "brown" -> BROWN;
            case "field" -> FIELD;
            case "mycelial" -> MYCELIAL;
            default -> RED;
        };
    }

    private static float[] rgbToHsl(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float l = (max + min) / 2f;

        if (max == min)
            return new float[]{0f, 0f, l};

        float delta = max - min;
        float s = l > 0.5f ? delta / (2f - max - min) : delta / (max + min);

        float h;
        if (max == r)
            h = ((g - b) / delta + (g < b ? 6 : 0)) / 6f;
        else if (max == g)
            h = ((b - r) / delta + 2) / 6f;
        else
            h = ((r - g) / delta + 4) / 6f;

        return new float[]{h, s, l};
    }
}
