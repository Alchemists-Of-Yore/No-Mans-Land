package com.farcr.nomansland.common.entity.buddy;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class BuddyJumpBehavior extends Behavior<Buddy> {
    public BuddyJumpBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_PRESENT), 10, 40);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Buddy buddy, long gameTime) {
        return buddy.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Buddy buddy, long gameTime) {
        if (buddy.onGround() && buddy.getRandom().nextInt(60) == 0) {
            buddy.getJumpControl().jump();
        }
    }
}
