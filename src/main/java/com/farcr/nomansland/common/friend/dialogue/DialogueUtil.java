package com.farcr.nomansland.common.friend.dialogue;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class DialogueUtil {
    public static final int FRIEND_MOON_TEXT_COLOR = 9276752;
    public static Registry<DialogueRegistry.DialoguePool> getDialogueRegistry(Level level, ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
        try {
            return level.registryAccess().registryOrThrow(resourceKey);
        } catch (IllegalStateException e) {
            throw new RuntimeException("Error obtaining dialogue registry: " + e);
        }
    }

    /* It's probably fine to make this recursive? lol??? */
    public static DialogueRegistry.DialoguePool getWeightedEntry(WeightedRandomList<DialogueRegistry.DialoguePool> list, RandomSource randomSource) {
        Optional<DialogueRegistry.DialoguePool> optionalDialogue = list.getRandom(randomSource);
        return optionalDialogue.orElseGet(() -> getWeightedEntry(list, randomSource));
    }
}
