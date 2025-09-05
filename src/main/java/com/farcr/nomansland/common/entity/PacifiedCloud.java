package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.client.particle.TranslucentDustParticleOptions;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class PacifiedCloud extends LingeringCloud {

    public PacifiedCloud(EntityType<? extends LingeringCloud> entityType, Level level) {
        super(entityType, level);
    }

    public PacifiedCloud(Level level, double x, double y, double z) {
        super(NMLEntities.PACIFIED_CLOUD.get(), level);
        setPos(x, y, z);
    }

    @Override
    public void tick() {
        super.tick();

        Level level = level();

        if (level.getBlockState(blockPosition().above((int) (getBbHeight() * 0.3))).isAir()) {
            Vec3 motion = getDeltaMovement();
            double dy = Mth.clamp(motion.y - 0.02, -0.04, 0);
            setDeltaMovement(motion.x, dy, motion.z);
            move(MoverType.SELF, getDeltaMovement());
        }
    }

    @Override
    public ParticleOptions getParticle(LevelAccessor levelAccessor) {
        return switch (levelAccessor.getRandom().nextInt(6)) {
            case 0 -> TranslucentDustParticleOptions.create(FastColor.ARGB32.color(51, 0xc6ff82));
            case 1 -> TranslucentDustParticleOptions.create(FastColor.ARGB32.color(51, 0xffdd82));
            case 2 -> TranslucentDustParticleOptions.create(FastColor.ARGB32.color(51, 0xfeb3bc));
            case 3 -> TranslucentDustParticleOptions.create(FastColor.ARGB32.color(51, 0xfe82ff));
            case 4 -> TranslucentDustParticleOptions.create(FastColor.ARGB32.color(51, 0xd682ff));
            default -> TranslucentDustParticleOptions.create(FastColor.ARGB32.color(51, 0x82ffc0));
        };
    }
}
