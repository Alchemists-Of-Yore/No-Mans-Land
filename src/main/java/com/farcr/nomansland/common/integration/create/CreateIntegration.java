package com.farcr.nomansland.common.integration.create;

import com.farcr.nomansland.common.registry.NMLFluids;
import com.simibubi.create.api.effect.OpenPipeEffectHandler;

public class CreateIntegration {
    public static void registerOpenPipeEffects() {
        OpenPipeEffectHandler.REGISTRY.register(NMLFluids.RESIN_OIL.get().getSource(), new ResinOilEffectHandler());
    }
}
