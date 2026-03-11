package com.farcr.nomansland.common.extension;

import org.apache.commons.lang3.NotImplementedException;

public interface LivingEntityExtension {
    default void nml$skipDroppingDeathLoot() throws NotImplementedException {
        throw new NotImplementedException();
    }

    default void nml$beginBellParalysis() {};
    default int nml$getBellParalysis() {
        return 0;
    };
}
