package com.farcr.nomansland.common.entity.living_pot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LivingPotWanderGoal extends WaterAvoidingRandomStrollGoal {

    private static final float STRAY_BIAS_CHANCE = 0.7F;
    private static final int STRAY_DISTANCE = 8;
    private static final double MAX_WANDER_DISTANCE = 48.0;

    private final LivingPot pot;

    public LivingPotWanderGoal(LivingPot pot, double speedModifier) {
        super(pot, speedModifier);
        this.pot = pot;
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        BlockPos home = pot.getHomePos();
        if (home == null) return super.getPosition();

        Vec3 homeVec = Vec3.atBottomCenterOf(home);
        double distFromHome = pot.position().subtract(homeVec).horizontalDistance();

        if (distFromHome >= MAX_WANDER_DISTANCE) {
            Vec3 pos = DefaultRandomPos.getPosTowards(pot, 10, 7, homeVec, Math.PI / 2);
            return pos != null ? pos : super.getPosition();
        }

        if (pot.getRandom().nextFloat() < 0.5F) {
            return super.getPosition();
        }

        if (pot.getRandom().nextFloat() >= STRAY_BIAS_CHANCE) {
            return super.getPosition();
        }

        Vec3 awayDir = pot.position().subtract(homeVec);
        double dist = awayDir.horizontalDistance();

        Vec3 biasTarget;
        if (dist > 0.5) {
            biasTarget = pot.position().add(awayDir.normalize().scale(STRAY_DISTANCE));
        } else {
            double angle = pot.getRandom().nextDouble() * Math.PI * 2;
            biasTarget = pot.position().add(Math.cos(angle) * STRAY_DISTANCE, 0, Math.sin(angle) * STRAY_DISTANCE);
        }

        Vec3 pos = DefaultRandomPos.getPosTowards(pot, 10, 7, biasTarget, Math.PI / 2);
        return pos != null ? pos : super.getPosition();
    }
}
