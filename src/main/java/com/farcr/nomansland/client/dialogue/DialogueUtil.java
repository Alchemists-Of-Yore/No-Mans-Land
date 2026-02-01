package com.farcr.nomansland.client.dialogue;

import com.farcr.nomansland.common.block.moonlight.DialogueRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DialogueUtil {
    public static Registry<DialogueRegistry.DialoguePool> getDialogueRegistry(Level level, ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
        try {
            return level.registryAccess().registryOrThrow(resourceKey);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Error obtaining dialogue registry: " + e);
        }
    }
}
