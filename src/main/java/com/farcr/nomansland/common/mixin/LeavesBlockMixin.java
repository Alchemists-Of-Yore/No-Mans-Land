package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NMLConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends BlockBehaviourMixin {

    @Override
    protected void getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (NMLConfig.WALK_THROUGH_LEAVES.get() && context instanceof EntityCollisionContext entityCollisionContext && entityCollisionContext.getEntity() != null) {
            cir.setReturnValue(Shapes.empty());
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (NMLConfig.WALK_THROUGH_LEAVES.get()) {
            entity.setSprinting(false);
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.7, 0.9, 0.7));
            entity.fallDistance *= 0.9F;
            if (entity.getDeltaMovement().lengthSqr() > 0.1) entity.playSound(SoundEvents.GRASS_STEP, 1, 1.25F);
        }
    }
}
