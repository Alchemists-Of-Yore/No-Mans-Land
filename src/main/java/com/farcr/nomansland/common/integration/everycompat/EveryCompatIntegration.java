package com.farcr.nomansland.common.integration.everycompat;

import com.farcr.nomansland.NoMansLand;
import net.mehvahdjukaar.every_compat.api.EveryCompatAPI;

public class EveryCompatIntegration {

    /*
     * This is a dirty "hack" because when every compat is not installed
     * it tries to import the `EveryCompatAPI` class on the mod init making it crash.
     */
    public static void register() {
        EveryCompatAPI.registerModule(new NMLEveryCompatModule(NoMansLand.MODID));
    }
}
