package com.farcr.nomansland.client;

import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientDialogueTracker {
    private ClientDialogueTracker() {}

    private static boolean heardAnyDialogue = false;
    private static final Set<ResourceLocation> heardOfferingDialogues = new HashSet<>();

    public static void acceptSync(boolean heardAny, Collection<ResourceLocation> heardOffering) {
        heardAnyDialogue = heardAny;
        heardOfferingDialogues.clear();
        heardOfferingDialogues.addAll(heardOffering);
    }

    public static boolean hasHeardAnyDialogue() {
        return heardAnyDialogue;
    }

    public static boolean hasHeardOfferingDialogue(ResourceLocation dialogueLocation) {
        return heardOfferingDialogues.contains(dialogueLocation);
    }

    public static List<ResourceLocation> getOfferingDialoguesForItem(Item item) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return List.of();
        return DialogueUtil.getOfferingDialogueLocations(minecraft.level.registryAccess(), item);
    }

    public static List<ResourceLocation> getOfferingDialoguesForEntity(EntityType<?> entityType) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null)
            return List.of();
        return DialogueUtil.getOfferingDialogueLocations(minecraft.level.registryAccess(), entityType);
    }
}
