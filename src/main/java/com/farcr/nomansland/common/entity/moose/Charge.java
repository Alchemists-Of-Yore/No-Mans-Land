package com.farcr.nomansland.common.entity.moose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.breeze.BreezeUtil;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class Charge extends Behavior<Moose> {
    public Charge() {
        super(Map.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.REGISTERED, MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Moose moose) {
        return moose.getBrain().getMemory(MemoryModuleType.ATTACK_COOLING_DOWN).isEmpty();
    }

    @Override
    protected void start(ServerLevel level, Moose moose, long gameTime) {
        LivingEntity target = moose.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).orElse(null);
        if (target != null)
            moose.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(target.position(), 1.7F, 1));
    }
}
