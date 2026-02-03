package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], FriendMoonRenderer.getFriendMoonOpacity());

            // Reset Text when the moon goes away
            if (FriendMoonRenderer.getFriendMoonOpacity() <= 0) {
                setCurrentState(null);
                return;
            }

            float deltaTime = deltaTracker.getGameTimeDeltaTicks();
            List<String> constructedText = currentState.progressText(deltaTime);

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
                    guiGraphics.drawString(font, text, leftPos, 0, DialogueUtil.FRIEND_MOON_TEXT_COLOR);
                    guiGraphics.pose().translate(0, -(TEXT_HEIGHT + 4), 0);
                }
            }
            guiGraphics.pose().popPose();
            RenderSystem.setShaderColor(shaderColor[0], shaderColor[1], shaderColor[2], 1f);
        }
    }
}
