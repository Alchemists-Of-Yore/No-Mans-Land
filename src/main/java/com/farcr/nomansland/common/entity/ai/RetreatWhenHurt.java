package com.farcr.nomansland.common.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class RetreatWhenHurt extends Behavior<Mob> {

    private final MemoryModuleType<LivingEntity> memory;

    public RetreatWhenHurt(MemoryModuleType<LivingEntity> memory) {
        super(Map.of(memory, MemoryStatus.VALUE_PRESENT));
        this.memory = memory;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob mob) {
        return mob.getHealth() < mob.getMaxHealth() / 2;
    }

    @Override
    protected void start(ServerLevel level, Mob mob, long gameTime) {
        Brain<?> brain = mob.getBrain();
        brain.getMemory(memory).ifPresent(target -> {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            brain.eraseMemory(MemoryModuleType.PATH);
            brain.setMemoryWithExpiry(MemoryModuleType.AVOID_TARGET, target, 20*10);
            mob.setTarget(null);
        });
    }
}
