package com.farcr.nomansland.common.entity.moose;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Stomp extends Behavior<Moose> {
    private static final int DISTANCE_XZ = 15;
    private static final int DISTANCE_Y = 20;
    private static final double KNOCKBACK_VERTICAL = (double)0.5F;
    private static final double KNOCKBACK_HORIZONTAL = (double)2.5F;
    public static final int COOLDOWN = 160;
    private static final int DURATION = 60;

    public Stomp() {
        super(ImmutableMap.of(
                MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.VALUE_PRESENT,
                MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT
        ), DURATION);
    }

    protected boolean checkExtraStartConditions(ServerLevel level, Moose moose) {
        return moose.closerThan(moose.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).get(), 5, 2);
    }

    protected boolean canStillUse(ServerLevel level, Moose moose, long gameTime) {
        return true;
    }

    protected void start(ServerLevel level, Moose moose, long gameTime) {
        moose.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, DURATION);
        System.out.println("stomp started");
    }

    protected void tick(ServerLevel level, Moose moose, long gameTime) {
        moose.getBrain().getMemory(MemoryModuleType.NEAREST_ATTACKABLE).ifPresent(
                target -> moose.getLookControl().setLookAt(target.position())
        );
    }

    @Override
    protected void stop(ServerLevel level, Moose moose, long gameTime) {
        moose.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, COOLDOWN);
        System.out.println("stomp stopped");
    }
}
