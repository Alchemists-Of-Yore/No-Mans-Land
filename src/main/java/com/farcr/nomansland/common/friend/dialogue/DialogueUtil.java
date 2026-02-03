package com.farcr.nomansland.common.friend.dialogue;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DialogueUtil {
    public static final int FRIEND_MOON_TEXT_COLOR = 9276752;
    public static Registry<DialogueRegistry.DialoguePool> getDialogueRegistry(Level level, ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
        try {
            return level.registryAccess().registryOrThrow(resourceKey);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Error obtaining dialogue registry: " + e);
        }
    }
}
