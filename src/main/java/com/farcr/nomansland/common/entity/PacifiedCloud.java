package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.client.particle.TranslucentDustParticleOptions;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class PacifiedCloud extends LingeringCloud {

    public PacifiedCloud(EntityType<? extends LingeringCloud> entityType, Level level) {
        super(entityType, level);
    }

    public PacifiedCloud(Level level, double x, double y, double z) {
        super(NMLEntities.PACIFIED_CLOUD.get(), level);
        setPos(x, y, z);
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
