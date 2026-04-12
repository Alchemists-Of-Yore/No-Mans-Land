package com.farcr.nomansland.common.world.structure;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public interface  StructurePlaceSettingsErosionHolder {
    default StructurePlaceSettings nml$setErosionType(StructureErosion.Type type) {
        throw new UnsupportedOperationException();
    }
    default StructureErosion.Type nml$getErosionType() {
        throw new UnsupportedOperationException();
    }
}
