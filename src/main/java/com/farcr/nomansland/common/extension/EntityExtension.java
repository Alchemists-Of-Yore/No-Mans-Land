package com.farcr.nomansland.common.extension;

import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Unique;

public interface EntityExtension {
    default void NML$setInspectionState(boolean isInspecting) throws NotImplementedException {
        throw new NotImplementedException();
    }
    default boolean NML$isBeingInspected() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default boolean NML$wasPreviouslyInspected() throws NotImplementedException {
        throw new NotImplementedException();
    }
}
