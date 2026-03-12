package com.farcr.nomansland.common.entity.cervidae.moose;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class MooseMovementData {

    private final Vector3f previousMotion = new Vector3f();
    private final Vector3f motion = new Vector3f();

    private final Vector3f movementDirection = new Vector3f();

    public MooseMovementData() {
    }

    public void update() {
        previousMotion.set(motion);
    }

    public void interpolate(float speed, float acceleration) {
        double x = Mth.approach(previousMotion.x, movementDirection.x * speed, acceleration);
        double y = Mth.approach(previousMotion.y, movementDirection.y * speed, acceleration);
        double z = Mth.approach(previousMotion.z, movementDirection.z * speed, acceleration);
        setMotion(x, y, z);
    }

    public void setMotion(Vec3 motion) {
        setMotion(motion.x, motion.y, motion.z);
    }

    public void setMotion(float x, float y, float z) {
        motion.set(x, y, z);
    }

    public void setMotion(double x, double y, double z) {
        motion.set(x, y, z);
    }

    public Vector3f getPreviousMotion() {
        return previousMotion;
    }

    public Vector3f getMotion() {
        return motion;
    }

    public Vector3f getMovementDirection() {
        return movementDirection;
    }

    public Vec3 getMotionVector() {
        return new Vec3(motion.x, motion.y, motion.z);
    }

    public float getMotionLength() {
        return motion.length();
    }
}