package com.farcr.nomansland.common.entity.cervidae.moose.deprecated;

import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;

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
