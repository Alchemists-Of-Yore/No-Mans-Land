package com.farcr.nomansland.client.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class DialogueState {
    public static final float DIALOGUE_SPEED = (1f / 60f) * 45f;
    public static final String TRANSLATABLE_COMPONENT = ".friend_moon.dialogue.";
    private static String translate(ResourceLocation location) {
        return location.getNamespace() + TRANSLATABLE_COMPONENT + location.getPath();
    }

    public double progress = 0d;
    public boolean doneTalking = false;
    public DialogueContainer originalDialogue;
    public DialogueContainer translateDialogue;

    public DialogueState(ResourceLocation location, DialogueRegistry.DialoguePool dialoguePool) {
        String defaultText = dialoguePool.text();

        originalDialogue = new DialogueContainer(defaultText);
        translateDialogue = new DialogueContainer(Language.getInstance().getOrDefault(translate(location), defaultText));
    }

    public String currentLatest;
    public static final List<String> illegalSpeakingCharacters = List.of(" ", "/", ",", ".");
    public boolean canSpeakCurrently() {
        for (String illegalChar : illegalSpeakingCharacters) {
            if (currentLatest != null && currentLatest.equals(illegalChar))
                return false;
        }
        return true;
    }

    public List<String> progressText(float deltaTime) {
        float conversionRate = ((float) translateDialogue.getTextLength() / Math.max(originalDialogue.getTextLength(), 1));
        progress += (deltaTime * DIALOGUE_SPEED) * conversionRate;
        List<String> stringList = translateDialogue.constructText((int) progress);

        // A bit unreliable but it should be fine
        doneTalking = translateDialogue.flagDoneConstructed;

        // Obtain last character as a string
        String lastString = stringList.getLast();
        if (lastString != null && !lastString.isEmpty()) {
            char latestCharacter = lastString.charAt(lastString.length() - 1);
            currentLatest = String.valueOf(latestCharacter);
        }
        return stringList;
    }
}