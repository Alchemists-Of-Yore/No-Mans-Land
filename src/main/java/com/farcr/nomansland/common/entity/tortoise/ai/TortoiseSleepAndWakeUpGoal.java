package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class TortoiseSleepAndWakeUpGoal extends Goal {
    private final Tortoise tortoise;
    private Level level;

    public TortoiseSleepAndWakeUpGoal(Tortoise tortoise) {
        this.tortoise = tortoise;
        this.level = tortoise.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        int lightLevel = this.level.getSkyDarken();
        return (lightLevel >= 3 && !tortoise.inShell() || lightLevel < 2 && tortoise.inShell()) && this.tortoise.getLastHurtByUUID() == null;
    }

    @Override
    public void start() {
        super.start();
        this.tortoise.retreatShell(!this.tortoise.inShell());
    }
}
