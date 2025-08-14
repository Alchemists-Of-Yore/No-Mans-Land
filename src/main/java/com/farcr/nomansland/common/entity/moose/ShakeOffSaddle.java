package com.farcr.nomansland.common.entity.moose;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class ShakeOffSaddle extends Behavior<Moose> {
    public ShakeOffSaddle() {
        super(Map.of());
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Moose moose) {
        return moose.isSaddled() && !moose.isPacified();
    }

    @Override
    protected void start(ServerLevel level, Moose moose, long gameTime) {
        moose.shakeOffSaddle();
    }
}
