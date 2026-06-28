package com.farcr.nomansland.common.entity.centipede;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

public class CentipedeMoveControl extends MoveControl {
    private final Centipede centipede;
    private float smoothSpeed;

    public CentipedeMoveControl(Centipede centipede) {
        super(centipede);
        this.centipede = centipede;
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO) {
            this.operation = Operation.WAIT;
            double dx = this.wantedX - this.mob.getX();
            double dy = this.wantedY - this.mob.getY();
            double dz = this.wantedZ - this.mob.getZ();
            double distSq = dx * dx + dy * dy + dz * dz;
            if (distSq < 2.5E-7) {
                this.mob.setZza(0.0F);
                return;
            }

            float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
            float turnErr = Mth.wrapDegrees(targetYaw - this.mob.getYRot());
            float maxTurn = this.centipede.isLunging() ? 22.0F : Mth.lerp(Math.min(1.0F, Math.abs(turnErr) / 180.0F), 4.0F, 11.0F);

            float headSpeed = this.centipede.getHeadSpeed();
            if (headSpeed > 0.03F) {
                float rMin = 1.3F + 0.13F * this.centipede.getSegments();
                float omegaMaxDeg = headSpeed / rMin * Mth.RAD_TO_DEG;
                maxTurn = Math.min(maxTurn, Math.max(2.0F, omegaMaxDeg));
            }

            float newYaw = this.mob.getYRot() + Mth.clamp(turnErr, -maxTurn, maxTurn);
            this.mob.setYRot(newYaw);

            float cosErr = Mth.cos(turnErr * Mth.DEG_TO_RAD);
            float speedGate = 0.6F + 0.4F * Math.max(0.0F, cosErr);
            float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            this.smoothSpeed = Mth.lerp(0.35F, this.smoothSpeed, speed * speedGate);
            this.mob.setSpeed(this.smoothSpeed);
        } else {
            this.mob.setZza(0.0F);
        }
    }
}
