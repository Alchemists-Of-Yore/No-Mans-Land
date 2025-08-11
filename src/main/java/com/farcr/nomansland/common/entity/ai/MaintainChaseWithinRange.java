package com.farcr.nomansland.common.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class MaintainChaseWithinRange extends Behavior<Mob> {

    private final double maxRange;

    public MaintainChaseWithinRange(double maxRange) {
        super(Map.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT));
        this.maxRange = maxRange;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        return true;
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Mob entity, long gameTime) {
        return true;
    }

    @Override
    protected void tick(ServerLevel level, Mob mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
            if (mob.distanceToSqr(target) > Mth.square(maxRange)) {
                brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
                brain.eraseMemory(MemoryModuleType.WALK_TARGET);
                brain.eraseMemory(MemoryModuleType.PATH);
                mob.setTarget(null);
            }
        });
    }
}

