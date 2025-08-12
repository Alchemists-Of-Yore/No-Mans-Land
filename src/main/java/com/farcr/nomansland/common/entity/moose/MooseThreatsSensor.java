package com.farcr.nomansland.common.entity.moose;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class MooseThreatsSensor extends NearestVisibleLivingEntitySensor {

    @Override
    protected boolean isMatchingEntity(LivingEntity attacker, LivingEntity target) {
        if (attacker instanceof Moose moose && moose.isPacified()) return false;
        return this.isClose(attacker, target) && isThreat(target) && Sensor.isEntityAttackable(attacker, target);
    }

    private boolean isThreat(LivingEntity target) {
        return target instanceof Monster || target instanceof Player;
    }

    private boolean isClose(LivingEntity attacker, LivingEntity target) {
        return attacker.distanceToSqr(target) <= 36;
    }

    @Override
    protected MemoryModuleType<LivingEntity> getMemory() {
        return MemoryModuleType.NEAREST_ATTACKABLE;
    }
}

