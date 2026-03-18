package com.farcr.nomansland.common.friend.dialogue;

import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class DialogueState {
    public static final float DIALOGUE_SPEED = (1f / 60f) * 45f;
    public static final String TRANSLATABLE_COMPONENT = ".friend_moon.";
    private static String translate(String registryName, ResourceLocation location) {
        return location.getNamespace() + TRANSLATABLE_COMPONENT + registryName + "." + location.getPath();
    }

    public double progress = 0d;
    public boolean doneTalking = false;
    public DialogueContainer originalDialogue;
    public DialogueContainer translateDialogue;

    public float ticks;
    public static final int FADE_TICKS = 20;
    public void setTicks(float newTicks) {
        this.ticks = newTicks;
    }
    public static final int GRADIENT_FADE_TICKS = 8;
    public float elapsedTicks = 0f;

    private boolean paused = false;
    public void pause() { paused = true; }
    public boolean isPaused() {
        return paused;
    }

    public @Nullable Integer overrideColor;
    public void setOverrideColor(int overrideColor) {
        this.overrideColor = overrideColor;
    }

    public DialogueState(
        ResourceLocation location,
        String registryName,
        DialoguePool dialoguePool
    ) {
        String defaultText = dialoguePool.text();
        originalDialogue = new DialogueContainer(defaultText);
        translateDialogue = new DialogueContainer(Language.getInstance()
                .getOrDefault(translate(registryName, location), defaultText));
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

    public void handleTime(float deltaTime) {
        if (!Minecraft.getInstance().isPaused()) {
            ticks -= deltaTime;
            elapsedTicks += deltaTime;
        }
    }

    public List<String> progressText(float deltaTime) {
        float conversionRate = ((float) translateDialogue.getTextLength() / Math.max(originalDialogue.getTextLength(), 1));
        if (!isPaused()) progress += (deltaTime * DIALOGUE_SPEED) * conversionRate;
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