package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class InkCloud extends LingeringCloud {

    public InkCloud(EntityType<? extends LingeringCloud> entityType, Level level) {
        super(entityType, level);
    }

    public InkCloud(Level level, double x, double y, double z) {
        super(NMLEntities.LINGERING_CLOUD.get(), level);
        setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount % 10 == 0) {
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox());
            for (LivingEntity entity : entities) {
                if (entity.isAffectedByPotions()) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30));
                    entity.removeEffect(MobEffects.INVISIBILITY);
                    entity.removeEffect(MobEffects.NIGHT_VISION);

                }
            }
        }
    }

    @Override
    public ParticleOptions getParticle() {
        int choice = (int) (Math.random() * 4);
        return switch (choice) {
            case 1 -> new DustParticleOptions(Vec3.fromRGB24(0x0b0a09).toVector3f(), 2);
            case 2 -> new DustParticleOptions(Vec3.fromRGB24(0x131110).toVector3f(), 2);
            case 3 -> new DustParticleOptions(Vec3.fromRGB24(0x111819).toVector3f(), 2);
            default -> new DustParticleOptions(Vec3.fromRGB24(0x1a1c1b).toVector3f(), 2);
        };
    }
}
