package com.farcr.nomansland.common.entity.goose;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class GooseFlight {

    private GooseFlight() {
    }

    public static void towardPoint(Goose goose, double targetX, double targetZ, double targetY,
                                   float speed, float turnRate, float accel, double climbCap) {
        float yaw = goose.getYRot();
        double dx = targetX - goose.getX();
        double dz = targetZ - goose.getZ();
        if (dx * dx + dz * dz > 0.25) {
            float desired = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            yaw = Mth.approachDegrees(yaw, desired, turnRate);
        }
        apply(goose, yaw, targetY, speed, accel, climbCap);
    }

    public static void alongHeading(Goose goose, Vec3 heading, double targetY,
                                    float speed, float turnRate, float accel, double climbCap) {
        float desired = (float) (Mth.atan2(heading.z, heading.x) * (180.0 / Math.PI)) - 90.0F;
        float yaw = Mth.approachDegrees(goose.getYRot(), desired, turnRate);
        apply(goose, yaw, targetY, speed, accel, climbCap);
    }

    public static void spiral(Goose goose, float spin, double targetY, float speed, float accel, double climbCap) {
        apply(goose, goose.getYRot() + spin, targetY, speed, accel, climbCap);
    }

    private static void apply(Goose goose, float yaw, double targetY, float speed, float accel, double climbCap) {
        goose.setYRot(yaw);
        goose.yBodyRot = yaw;
        goose.yHeadRot = yaw;
        float rad = yaw * ((float) Math.PI / 180F);
        Vec3 forward = new Vec3(-Mth.sin(rad), 0, Mth.cos(rad));
        double climb = Mth.clamp((targetY - goose.getY()) * 0.1, -climbCap, climbCap);
        Vec3 desired = forward.scale(speed).add(0, climb, 0);
        Vec3 velocity = goose.getDeltaMovement();
        goose.setDeltaMovement(velocity.add(desired.subtract(velocity).scale(accel)));
    }
}
