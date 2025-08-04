package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class TortoiseSearchForDangerGoal extends Goal {
    private final Tortoise tortoise;

    public TortoiseSearchForDangerGoal(Tortoise tortoise) {
        this.tortoise = tortoise;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.tortoise.level().getGameTime();
        return this.tortoise.inShell() && (gameTime - this.tortoise.getHurtWhen() > 700L) && this.tortoise.getLastHurtByUUID() != null;
    }

    @Override
    public void start() {
        super.start();
        List<Entity> entityList = this.tortoise.level().getEntities(this.tortoise, this.tortoise.getBoundingBox().inflate(10), entity -> this.tortoise.getLastHurtByUUID() != null && this.tortoise.getLastHurtByUUID() == entity.getUUID());
        if (!entityList.isEmpty()) {
            this.tortoise.setHurtWhen(this.tortoise.level().getGameTime());
            this.tortoise.retreatShell(true);
            this.tortoise.setSearching(false);
        } else {
            this.tortoise.retreatShell(false);
            this.tortoise.setSearching(false);
            this.tortoise.setLastHurtByUUID(null);
        }
    }
}
