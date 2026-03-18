package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.client.renderer.friend.FriendMoonRenderer;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class DialogueRenderer {
    private static DialogueState currentState;
    public static DialogueState getCurrentState() {
        return currentState;
    }

    public static void setCurrentState(DialogueState newState) {
        currentState = newState;
    }

    public static final int TEXT_HEIGHT = 9;
    public static float actionBarDisplacement = 0f;

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (currentState != null) {
            Minecraft mc = Minecraft.getInstance();

            float gameWidth = guiGraphics.guiWidth();
            float[] shaderColor = RenderSystem.getShaderColor();

            float deltaTime = deltaTracker.getGameTimeDeltaTicks();

            float initialFade = Math.min(currentState.elapsedTicks / DialogueState.GRADIENT_FADE_TICKS, 1);
            float totalOpacity = Math.min(initialFade, (DialogueState.FADE_TICKS + currentState.ticks) / DialogueState.FADE_TICKS);
            currentState.handleTime(deltaTime);

            // Reset Text when the moon goes away
            if (initialFade >= 1 && totalOpacity <= 0.1f) {
                setCurrentState(null);
                return;
            }

            float lastOpacity = shaderColor[3];
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], totalOpacity);
            List<String> constructedText = currentState.progressText(deltaTime);

            Font font = mc.gui.getFont();
            guiGraphics.pose().pushPose();

            float moveSpeed = 0.5f;
            float t = (float) (1f - Math.exp(deltaTime * -moveSpeed));

            float moveTo = (mc.gui.overlayMessageTime > 0) ? (TEXT_HEIGHT * 2) : 0;
            actionBarDisplacement = Mth.lerp(t, actionBarDisplacement, moveTo);

            int yShift = Math.max(mc.gui.leftHeight, mc.gui.rightHeight);
            float yPosition = (guiGraphics.guiHeight() - Math.max(yShift, 72)) - actionBarDisplacement;

            // Background Gradient
            guiGraphics.fillGradient(0, (int) (yPosition - (TEXT_HEIGHT * 3)),
                guiGraphics.guiWidth(), guiGraphics.guiHeight(),
                FastColor.ARGB32.colorFromFloat(0, 0, 0, 0),
                FastColor.ARGB32.colorFromFloat(totalOpacity, 0, 0, 0)
            );

            guiGraphics.pose().translate(0, yPosition, 100.0F);

            float percentageUsable = .9f;
            float center = (gameWidth / 2f);

            ArrayList<String> totalStringSplits = new ArrayList<>();
            for (int i = 0; i < constructedText.size(); i++) {
                String text = constructedText.get(i);
                if (!text.isEmpty()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String[] splitText = text.split(" ");
                    for (int j = 0; j < splitText.length; j++) {
                        stringBuilder.append(splitText[j]);
                        if (j < splitText.length - 1)
                            stringBuilder.append(" ");
                        float rightPos = center + font.width(stringBuilder.toString()) / 2f;
                        if (rightPos > (gameWidth * percentageUsable) || (j >= splitText.length - 1)) {
                            totalStringSplits.add(stringBuilder.toString());
                            stringBuilder = new StringBuilder();
                        }
                    }
                }
            }
            for (int i = (totalStringSplits.size() - 1); i >= Math.max(0, totalStringSplits.size() - 3); i--) {
                String text = totalStringSplits.get(i);
                int leftPos = (int) (center - (font.width(text) / 2f));

                double alpha = mc.options.textBackgroundOpacity().get();
                if (alpha > 0) {
                    int padding = 2;
                    guiGraphics.fill(
                        leftPos - padding, -padding,
                        leftPos + font.width(text) + padding, TEXT_HEIGHT + padding,
                        FastColor.ARGB32.colorFromFloat((float) alpha, 0f, 0f, 0f)
                    );
                }

                guiGraphics.drawString(font, text, leftPos, 0,
                    (currentState.overrideColor != null) ? currentState.overrideColor : DialogueUtil.FRIEND_MOON_TEXT_COLOR);
                guiGraphics.pose().translate(0, -(TEXT_HEIGHT + 4), 0);
            }
            guiGraphics.pose().popPose();
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], lastOpacity);
        }
    }
}
