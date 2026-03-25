package com.farcr.nomansland.utility;

import net.minecraft.util.Mth;

public class SmootherDouble {
    private double value = 0;
    private double target = 0;
    private double strength = 0;

    public void reset() {
        this.value = 0;
        this.target = 0;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public void deltaTarget(double delta) {
        this.target += delta;
    }

    public void setStrength(double strength) {
        this.strength = Math.clamp(strength, 0, 1);
    }

    private double getDelta(double dt) {
        return (1 - this.strength) *
                (this.target - this.value) *
                Mth.lerp(this.strength, 1, Math.min(dt, 1));
    }

    public double getUpdatedDelta(double dt) {
        double diff = this.getDelta(dt);
        this.value += diff;
        return diff;
    }

    public double getUpdatedValue(double dt) {
        this.value += this.getDelta(dt);
        return this.value;
    }
}
