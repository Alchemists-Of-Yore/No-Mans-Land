package com.farcr.nomansland.common.entity.clod.ai;

import com.farcr.nomansland.common.entity.clod.Clod;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ClodInvestigateGoal extends Goal {
    private final Clod clod;
    private final double speedModifier;

    public ClodInvestigateGoal(Clod clod, double speedModifier) {
        this.clod = clod;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.clod.isFleeing() || !this.clod.hasLostThreat()) return false;
        return this.clod.findThreat(this.clod.leaveRange()) == null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.clod.isFleeing() || !this.clod.hasLostThreat()) return false;
        if (this.clod.findThreat(this.clod.leaveRange()) != null) return false;
        return !this.clod.getNavigation().isDone();
    }

    @Override
    public void start() {
        Vec3 target = this.clod.getLastThreatPos();
        if (target != null) {
            this.clod.getNavigation().moveTo(target.x, target.y, target.z, this.speedModifier);
        }
    }

    @Override
    public void stop() {
        this.clod.clearLostThreat();
    }
}
