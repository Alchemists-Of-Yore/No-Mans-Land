package com.farcr.nomansland.common.entity.goose;

import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

public class Intimidate extends Behavior<Goose> {
    public Intimidate() {
        super(Map.of());
    }
}
