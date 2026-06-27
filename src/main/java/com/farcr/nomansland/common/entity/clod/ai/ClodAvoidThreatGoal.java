package com.farcr.nomansland.common.entity.clod.ai;

import com.farcr.nomansland.common.entity.clod.Clod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ClodAvoidThreatGoal extends Goal {
    private final Clod clod;
    private final double speedModifier;
    private final PathNavigation navigation;
    private LivingEntity threat;
    private Path path;

    public ClodAvoidThreatGoal(Clod clod, double speedModifier) {
        this.clod = clod;
        this.speedModifier = speedModifier;
        this.navigation = clod.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!this.clod.isFleeing()) return false;
        this.threat = this.clod.findThreat(this.clod.leaveRange());
        if (this.threat == null) return false;
        Vec3 away = DefaultRandomPos.getPosAway(this.clod, 16, 7, this.threat.position());
        if (away == null) return false;
        if (this.threat.distanceToSqr(away.x, away.y, away.z) < this.threat.distanceToSqr(this.clod)) return false;
        this.path = this.navigation.createPath(away.x, away.y, away.z, 0);
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.clod.isFleeing() && !this.navigation.isDone();
    }

    @Override
    public void start() {
        this.navigation.moveTo(this.path, this.speedModifier);
    }

    @Override
    public void stop() {
        this.threat = null;
    }

    @Override
    public void tick() {
        LivingEntity current = this.clod.findThreat(this.clod.leaveRange());
        if (current != null) {
            this.clod.getLookControl().setLookAt(current);
            if (this.navigation.isDone()) {
                Vec3 away = DefaultRandomPos.getPosAway(this.clod, 16, 7, current.position());
                if (away != null) {
                    this.navigation.moveTo(away.x, away.y, away.z, this.speedModifier);
                }
            }
        }
    }
}
