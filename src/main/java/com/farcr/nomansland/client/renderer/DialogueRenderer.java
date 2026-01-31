package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class DialogueRenderer {
    public static float DIALOGUE_SPEED = (1f / 60f); // 1 Character Per Second

    public static class DialogueState {
        public static final String TRANSLATABLE_COMPONENT = ".friend_moon.dialogue.";
        private static String translate(ResourceLocation location) {
            return location.getNamespace() + TRANSLATABLE_COMPONENT + location.getPath();
        }

        public double progress = 0d;
        public static final List<String> DELIMITERS = List.of("/", "&PlayerName");

        private ArrayList<String> textList;

        public DialogueState(ResourceLocation location, DialogueRegistry.DialoguePool dialoguePool) {
            String defaultText = dialoguePool.text();
            Language language = Language.getInstance();

            ArrayList<String> finalList = new ArrayList<>();
            finalList.add(language.getOrDefault(translate(location), defaultText));

            // Split text based on delimiters
            for (String delimiter : DELIMITERS) {
                List<String> tempList = List.copyOf(finalList);

                finalList.clear();
                for (String substring : tempList) {
                    finalList.addAll(Arrays.asList(
                        substring.split("((?=" + delimiter + ")|(?<=" + delimiter + "))")
                    ));
                }
            }

            // hacky fix lol avert your eyes
            for (int i = 0; i < finalList.size(); i++) {
                if (finalList.get(i).contains("/")) {
                    StringBuilder setString = new StringBuilder();
                    int multiplier = 4;
                    setString.append("/".repeat(Math.max(0, (finalList.get(i).length() * multiplier))));
                    finalList.set(i, setString.toString());
                }
            }
            this.textList = finalList;
        }

        public List<String> constructText() {
            List<String> localText = new ArrayList<>(List.of());
            int i = 0;
            if (textList != null) {
                int totalText = 0;
                String totalString = "";
                while (i < textList.size()) {
                    if (progress < totalText)
                        break;
                    String subString = textList.get(i);
                    if (subString.contains("&PlayerName"))
                        subString = Minecraft.getInstance().getUser().getName();
                    if (!subString.contains("/"))
                        totalString += subString.substring(0, Math.min((int) (progress - totalText), subString.length()));
                    totalText += subString.length();
                    i++;
                }
                localText.addAll(Arrays.asList(totalString.split("\\n")));
            }
            return localText;
        }
    }
    private static DialogueState currentState;
    public static void setCurrentState(DialogueState newState) {
        currentState = newState;
    }

    public static final int TEXT_HEIGHT = 9;
    public static float actionBarDisplacement = 0f;

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (currentState != null) {
            Minecraft mc = Minecraft.getInstance();

            float gameWidth = guiGraphics.guiWidth();
            List<String> constructedText = currentState.constructText();
            float deltaTime = mc.getTimer().getGameTimeDeltaTicks();
            currentState.progress += deltaTime * (DIALOGUE_SPEED * 45f);

            Font font = mc.gui.getFont();
            guiGraphics.pose().pushPose();

            float moveSpeed = 0.5f;
            float t = (float) (1f - Math.exp(deltaTime * -moveSpeed));

            float moveTo = (mc.gui.overlayMessageTime > 0) ? (TEXT_HEIGHT * 2) : 0;
            actionBarDisplacement = Mth.lerp(t, actionBarDisplacement, moveTo);

            int yShift = Math.max(mc.gui.leftHeight, mc.gui.rightHeight);
            guiGraphics.pose().translate(0, (float)(guiGraphics.guiHeight() - Math.max(yShift, 72)) - actionBarDisplacement, 100.0F);
            for (int i = (constructedText.size() - 1); i >= 0; i--) {
                String text = constructedText.get(i);
                if (!text.isEmpty()) {
                    int leftPos = (int) ((gameWidth / 2f) - (font.width(text) / 2f));
                    double alpha = mc.options.textBackgroundOpacity().get();
                    if (alpha > 0) {
                        int padding = 2;
                        guiGraphics.fill(
                            leftPos - padding, -padding,
                            leftPos + font.width(text) + padding, TEXT_HEIGHT + padding,
                            FastColor.ARGB32.colorFromFloat((float) alpha, 0f, 0f, 0f)
                        );
                    }
                    guiGraphics.drawString(font, text, leftPos, 0, 9276752);
                    guiGraphics.pose().translate(0, -(TEXT_HEIGHT + 4), 0);
                }
            }
            guiGraphics.pose().popPose();
        }
    }
}
