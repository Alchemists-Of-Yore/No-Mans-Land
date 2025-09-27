package com.farcr.nomansland.common.entity.ai;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class LeapAtTargetBehavior extends Behavior<LivingEntity> {

    private final MemoryModuleType<LivingEntity> target;

    public LeapAtTargetBehavior(MemoryModuleType<LivingEntity> target) {
        super(Map.of(target, MemoryStatus.VALUE_PRESENT));
        this.target = target;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, LivingEntity entity) {
        if (entity.hasControllingPassenger()) {
            return false;
        } else {
            LivingEntity target = entity.getBrain().getMemory(this.target).orElse(null);
            if (target == null) {
                return false;
            } else {
                double distance = entity.distanceToSqr(target);
                if (distance > 4 && distance < 16) {
                    return entity.onGround() && entity.getRandom().nextInt(Mth.positiveCeilDiv(5, 2)) == 0;
                } else {
                    return false;
                }
            }
        }    
    }

    @Override
    protected boolean canStillUse(ServerLevel level, LivingEntity entity, long gameTime) {
        return !entity.onGround();
    }

    @Override
    protected void start(ServerLevel level, LivingEntity entity, long gameTime) {
        LivingEntity target = entity.getBrain().getMemory(this.target).orElse(null);

        Vec3 vec3 = new Vec3(target.getX() - entity.getX(), 0, target.getZ() - entity.getZ());
        if (vec3.lengthSqr() > 1.0E-7) {
            vec3 = vec3.normalize().scale(0.4).add(entity.getDeltaMovement().scale(0.2));
        }

        entity.setDeltaMovement(vec3.x, 0.4, vec3.z);
    }
}
