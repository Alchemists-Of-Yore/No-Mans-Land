package com.farcr.nomansland.common.entity.goose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
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
            yaw = Mth.approachDegrees(yaw, yawTo(dx, dz), turnRate);
        }
        apply(goose, yaw, targetY, speed, accel, climbCap, climbCap);
    }

    public static void alongHeading(Goose goose, Vec3 heading, double targetY,
                                    float speed, float turnRate, float accel, double climbCap) {
        float yaw = Mth.approachDegrees(goose.getYRot(), yawTo(heading.x, heading.z), turnRate);
        apply(goose, yaw, targetY, speed, accel, climbCap, climbCap);
    }

    public static void spiral(Goose goose, float spin, double targetY, float speed, float accel, double climbCap) {
        apply(goose, goose.getYRot() + spin, targetY, speed, accel, climbCap, climbCap);
    }

    public static void approach(Goose goose, double targetX, double targetZ, double targetY,
                                float speed, float turnRate, float accel, double climbCap, double descentCap) {
        float yaw = goose.getYRot();
        double dx = targetX - goose.getX();
        double dz = targetZ - goose.getZ();
        if (dx * dx + dz * dz > 0.04) {
            yaw = Mth.approachDegrees(yaw, yawTo(dx, dz), turnRate);
        }
        apply(goose, yaw, targetY, speed, accel, climbCap, descentCap);
    }

    public static double terrainAhead(ServerLevel level, double x, double z, double dirX, double dirZ, int near, int far) {
        int here = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
        int n = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x + dirX * near), Mth.floor(z + dirZ * near));
        int f = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x + dirX * far), Mth.floor(z + dirZ * far));
        return Math.max(here, Math.max(n, f));
    }

    private static void apply(Goose goose, float yaw, double targetY, float speed, float accel, double climbCap, double descentCap) {
        goose.setYRot(yaw);
        goose.yBodyRot = yaw;
        goose.yHeadRot = yaw;
        float rad = yaw * ((float) Math.PI / 180F);
        Vec3 forward = new Vec3(-Mth.sin(rad), 0, Mth.cos(rad));
        double raw = (targetY - goose.getY()) * 0.1;
        double climb = raw >= 0 ? Math.min(raw, climbCap) : Math.max(raw, -descentCap);
        double cap = climb >= 0 ? climbCap : descentCap;
        double fraction = cap > 1.0E-4 ? climb / cap : 0.0;
        double effSpeed = climb >= 0 ? speed * (1.0 - 0.35 * fraction) : speed * (1.0 + 0.25 * -fraction);
        Vec3 desired = forward.scale(effSpeed).add(0, climb, 0);
        Vec3 velocity = goose.getDeltaMovement();
        goose.setDeltaMovement(velocity.add(desired.subtract(velocity).scale(accel)));
    }

    private static float yawTo(double dx, double dz) {
        return (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
    }
}
