package com.farcr.nomansland.common.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class PacifiedAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
    public PacifiedAttackGoal(Mob mob) {
        super(mob, LivingEntity.class, 0, true, true, PacifiedAttackGoal::isHostile);
    }

    private static boolean isHostile(LivingEntity livingEntity) {
        return livingEntity instanceof Enemy && !(livingEntity instanceof Creeper);
    }

    @Override
    public void start() {
        super.start();
        mob.setAggressive(true);
    }
}
