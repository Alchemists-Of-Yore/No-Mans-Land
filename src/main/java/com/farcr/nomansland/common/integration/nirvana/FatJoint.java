package com.farcr.nomansland.common.integration.nirvana;

import com.farcr.nomansland.common.entity.bombs.Explosive;
import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import galena.nirvana.world.THCCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class FatJoint extends Explosive {

    public FatJoint(EntityType<? extends ThrowableBombEntity> entityType, Level level) {
        super(entityType, level);
    }

    public FatJoint(Level level, double x, double y, double z) {
        super(NirvanaIntegration.FAT_JOINT.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected void explode() {
        if (!level().isClientSide) THCCloud.spawnCloud(level(), position(), 1.25F, 10, 40);
        discard();
    }
}
