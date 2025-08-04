package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class TortoiseStayAroundHomeGoal extends Goal {
    private final Tortoise tortoise;
    private final double speedModifier;
    private boolean stuck;
    private int closeToHomeTryTicks;

    public TortoiseStayAroundHomeGoal(Tortoise tortoise, double speedModifier) {
        this.tortoise = tortoise;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        return this.tortoise.getHomePos() != null && !this.tortoise.getHomePos().closerToCenterThan(this.tortoise.position(), 10.0) && !this.tortoise.inShell();
    }

    @Override
    public void start() {
        this.tortoise.setGoingHome(true);
    }

    @Override
    public void tick() {
        BlockPos blockpos = this.tortoise.getHomePos();
        if (blockpos == null)
            return;
        if (this.tortoise.getNavigation().isDone()) {
            Vec3 vec3 = Vec3.atBottomCenterOf(blockpos);
            if (vec3 == null) {
                this.stuck = true;
                return;
            }
            this.tortoise.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.tortoise.setGoingHome(false);
    }

    @Override
    public boolean canContinueToUse() {
        return this.tortoise.getHomePos() != null && !this.tortoise.getHomePos().closerToCenterThan(this.tortoise.position(), 10);
    }
}
