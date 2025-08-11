package com.farcr.nomansland.common.effect;

import com.farcr.nomansland.common.entity.ai.EnemyAttackGoal;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;

public class PacifiedEffect extends MobEffect {
    public PacifiedEffect(MobEffectCategory category) {
        super(category, 1);
        this.particleFactory = i -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xc6ff82));
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide && livingEntity.tickCount % 5 == 0) {
            RandomSource random = livingEntity.getRandom();
            ColorParticleOption particle =  switch (livingEntity.level().getRandom().nextInt(6)) {
                case 0 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xc6ff82));
                case 1 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xffdd82));
                case 2 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xfeb3bc));
                case 3 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xfe82ff));
                case 4 -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0xd682ff));
                default -> ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(51, 0x82ffc0));
            };

            double x = livingEntity.getX() + (random.nextDouble() - 0.5) * livingEntity.getBbWidth();
            double y = livingEntity.getY() + random.nextDouble() * livingEntity.getBbHeight();
            double z = livingEntity.getZ() + (random.nextDouble() - 0.5) * livingEntity.getBbWidth();

            ((ServerLevel) livingEntity.level()).sendParticles(particle, x, y, z, 0, 0, 0, 0, 0);
        }

        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int amplifier) {
        super.onEffectAdded(livingEntity, amplifier);

        if (livingEntity.getType().getTags().toList().contains(NMLTags.CANNOT_BE_PACIFIED)) {
            livingEntity.removeEffect(NMLEffects.PACIFIED);
            return;
        }

        if (livingEntity instanceof Monster monster) {
            for (WrappedGoal goal : monster.targetSelector.getAvailableGoals()) {
                if (goal.getGoal() instanceof EnemyAttackGoal) {
                    return;
                }
            }

            monster.targetSelector.addGoal(0, new EnemyAttackGoal(monster));
        } else {
            if (livingEntity instanceof NeutralMob neutralMob) neutralMob.stopBeingAngry();
            livingEntity.removeEffect(NMLEffects.PACIFIED);
        }
    }
}
