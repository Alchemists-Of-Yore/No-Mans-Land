package com.farcr.nomansland.common.entity.moose;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Charge extends Behavior<Moose> {
    private static final int DISTANCE_XZ = 15;
    private static final int DISTANCE_Y = 20;
    private static final double KNOCKBACK_VERTICAL = (double)0.5F;
    private static final double KNOCKBACK_HORIZONTAL = (double)2.5F;
    public static final int COOLDOWN = 60;
    private static final int DURATION = 60;
    private final MemoryModuleType<LivingEntity> targetMemory;

    public Charge(MemoryModuleType<LivingEntity> targetMemory) {
        super(ImmutableMap.of(
                targetMemory, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT
        ), DURATION);

        this.targetMemory = targetMemory;
    }

    protected boolean checkExtraStartConditions(ServerLevel level, Moose moose) {
        return moose.closerThan(moose.getBrain().getMemory(targetMemory).get(), 5, 2);
    }

    protected boolean canStillUse(ServerLevel level, Moose moose, long gameTime) {
        return true;
    }

    protected void start(ServerLevel level, Moose moose, long gameTime) {
        moose.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, DURATION);
        System.out.println("charge started");
    }

    protected void tick(ServerLevel level, Moose moose, long gameTime) {
        moose.getBrain().getMemory(targetMemory).ifPresent(
                target -> moose.getLookControl().setLookAt(target.position())
        );
    }

    @Override
    protected void stop(ServerLevel level, Moose moose, long gameTime) {
        moose.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, COOLDOWN);
        System.out.println("charge stopped");
    }
}
