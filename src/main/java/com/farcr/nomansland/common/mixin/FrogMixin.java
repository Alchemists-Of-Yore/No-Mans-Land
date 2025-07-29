package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLMobVariants;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frog.class)
public class FrogMixin {
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Frog self = (Frog) (Object) this;
        if (level.getBiome(self.blockPosition()).is(NMLTags.SPAWNS_MUD_FROGS)) {
            self.setVariant(NMLMobVariants.MUD);
        }
    }
}
