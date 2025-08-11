package com.farcr.nomansland.common.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class StartChasingWhenHurt<T extends Mob> extends Behavior<T> {

    public StartChasingWhenHurt() {
        super(Map.of(
                MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED
        ), 20);
    }

    @Override
    protected void start(ServerLevel level, T mob, long gameTime) {
        mob.getBrain().getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent(attacker -> {
            if (attacker.isAlive()) {
                mob.getBrain().setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, attacker.getUUID(), 20 * 60 * 24 * 2);
                mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, attacker);
            }
        });
    }
}

