package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.saved_data.WardedSpacesData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(RandomStrollGoal.class)
public class RandomStrollGoalMixin {
    @Shadow @Final protected PathfinderMob mob;

    @Inject(method = "getPosition", at = @At("HEAD"), cancellable = true)
    private void getPosition(CallbackInfoReturnable<Vec3> cir) {
        if (mob instanceof Monster && mob.level() instanceof ServerLevel serverLevel) {
            WardedSpacesData wardedSpacesData = serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                    () -> new WardedSpacesData(new ArrayList<>(), new ArrayList<>()), WardedSpacesData::load), WardedSpacesData.NAME);

            if (wardedSpacesData.isWarded(mob.blockPosition())) {
                BlockPos effigyPos = wardedSpacesData.getAffectingEffigyAt(mob.blockPosition()).orElse(null);
                if (effigyPos != null) {
                    int effigyRange = wardedSpacesData.ranges.get(wardedSpacesData.positions.indexOf(effigyPos));

                    Vec3 newPosition = LandRandomPos.getPosAway(mob, mob.getRandom().nextInt(effigyRange / 4, effigyRange + effigyRange / 4), 10, Vec3.atCenterOf(effigyPos));
                    if (newPosition != null) cir.setReturnValue(newPosition);
                }
            }
        }
    }
}
