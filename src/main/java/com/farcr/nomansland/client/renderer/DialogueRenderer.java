package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

public class DialogueRenderer {
    public static float DIALOGUE_SPEED = (1f / 120f); // 1 Character Per Second

    public static class DialogueState {
        public static final String TRANSLATABLE_COMPONENT = ".friend_moon.dialogue.";
        private static String translate(ResourceLocation location) {
            return location.getNamespace() + TRANSLATABLE_COMPONENT + location.getPath();
        }

        public double progress = 0d;
        public int textLength = 0;
        public String text = "";

        public DialogueState(ResourceLocation location, DialogueRegistry.DialoguePool dialoguePool) {
            String defaultText = dialoguePool.text();
            Language language = Language.getInstance();
            text = language.getOrDefault(translate(location), defaultText);
            textLength = defaultText.length();
        }

        public String getText() {
            return text.substring(0, (int) (Math.min(progress, 1) * textLength));
        }
    }
    private static DialogueState currentState;
    public static void setCurrentState(DialogueState newState) {
        currentState = newState;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (currentState != null) {
            Minecraft mc = Minecraft.getInstance();

            float gameWidth = guiGraphics.guiWidth();
            String text = currentState.getText();
            currentState.progress += (mc.getTimer().getGameTimeDeltaTicks() / 1f) * DIALOGUE_SPEED;

            Font font = mc.gui.getFont();

            int yShift = Math.max(mc.gui.leftHeight, mc.gui.rightHeight) + 9;
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, (float)(guiGraphics.guiHeight() - Math.max(yShift, 68)), 100.0F);

            int leftPos = (int) ((gameWidth / 2f) - (font.width(text) / 2f));
            double i = mc.options.textBackgroundOpacity().get();
            if (i > 0) {
                int padding = 2;
                guiGraphics.fill(
                    leftPos - padding, -padding,
                    leftPos + font.width(text) + padding, 9 + padding,
                    FastColor.ARGB32.colorFromFloat((float) i, 0f, 0f, 0f)
                );
            }
            guiGraphics.drawString(font, text, leftPos, 0, 9276752);
            guiGraphics.pose().popPose();
        }
    }
}
