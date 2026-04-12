package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.structure.StructureErosion;
import com.farcr.nomansland.common.world.structure.StructurePlaceSettingsErosionHolder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePlaceSettings.class)
public abstract class StructurePlaceSettingsMixin implements StructurePlaceSettingsErosionHolder {
    @Unique
    private StructureErosion.Type nml$erosionType;

    @Inject(method = "copy", at = @At("RETURN"))
    public void nml$copy(CallbackInfoReturnable<StructurePlaceSettings> cir) {
        ((StructurePlaceSettingsErosionHolder) cir.getReturnValue()).nml$setErosionType(nml$erosionType);
    }

    @Unique
    public StructurePlaceSettings nml$setErosionType(StructureErosion.Type type) {
        this.nml$erosionType = type;
        return (StructurePlaceSettings) (Object)this;
    }
    @Unique
    public StructureErosion.Type nml$getErosionType() {
        if (nml$erosionType == null) return StructureErosion.Type.NONE;
        return nml$erosionType;
    }
}
