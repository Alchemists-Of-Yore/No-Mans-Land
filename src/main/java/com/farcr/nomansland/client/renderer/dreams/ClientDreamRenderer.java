package com.farcr.nomansland.client.renderer.dreams;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;

import java.util.List;

public class ClientDreamRenderer implements AutoCloseable {
    public static final int MAX_SLEEP_TICKS = 100;

    public static ClientDreamRenderer INSTANCE;
    public static ClientDreamRenderer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ClientDreamRenderer();
        return INSTANCE;
    }

    @Override
    public void close() {
        clientEndDream();
    }

    public static void destroy() {
        if (INSTANCE == null)
            return;
        INSTANCE.close();
        INSTANCE = null;
    }

    private DreamType dream;
    public void clientEndDream() {
        dream = null;
        renderer = null;
    }

    public void clientSetDream(DreamType dream) {
        clientEndDream();
        this.dream = dream;
    }

    public static final List<Class<? extends Screen>> blacklistedScreens = List.of(
        InventoryScreen.class
    );
    public static boolean isBlacklistedScreen(Screen guiScreen) {
        return guiScreen != null && blacklistedScreens.contains(guiScreen.getClass());
    }

    private IDreamRenderer renderer;
    public IDreamRenderer getRenderer() {
        if (dream.dreamRenderer != null && renderer == null)
            renderer = dream.dreamRenderer.get();
        return renderer;
    }

    public void tick() {
        if (!clientIsDreaming()) {
            clientEndDream();
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            // Clear Dreams
            if (!player.isAlive())
                clientEndDream();

            if (dream != null) getDreamClientInstance()
                .tick(Minecraft.getInstance().level);

            if (dream != null && DreamManager.innerDreaming(dream, player) && getRenderer() != null) {
                getRenderer().tick();
            }
        }
    }

    public DreamType getDream() {
        return dream;
    }

    DreamType.DreamTypeInstance clientInstance;
    public DreamType.DreamTypeInstance getDreamClientInstance() {
        if (clientInstance == null)
            clientInstance = dream.instanceSupplier.get();
        return clientInstance;
    }

    public boolean clientIsDreaming() {
        return (dream != null);
    }

    public int getRawTicks() {
        return Minecraft.getInstance()
            .player.getSleepTimer();
    }

    public boolean dreamShouldRender() {
        if (clientIsDreaming()) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (Minecraft.getInstance().screen instanceof ReceivingLevelScreen) {
                storedTicks = 0f;
                return true;
            }
            return DreamManager.innerDreaming(dream, player);
        }
        return false;
    }

    private float storedTicks = 0f;
    public float getSleepTicks(DeltaTracker deltaTracker) {
        if (dreamShouldRender()) {
            float deltaTicks = deltaTracker.getGameTimeDeltaTicks();
            if (Minecraft.getInstance().isPaused()) deltaTicks = 0f;
            storedTicks += (deltaTicks / 2.5f);
            return Math.max(0f, MAX_SLEEP_TICKS - storedTicks);
        }
        storedTicks = 0f;
        return getRawTicks();
    }

    public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        ClientDreamRenderer manager = ClientDreamRenderer.getInstance();
        if (manager.clientIsDreaming()) {
            float alpha = (manager.getSleepTicks(deltaTracker) / MAX_SLEEP_TICKS);
            if (manager.getRenderer() != null && manager.dreamShouldRender())
                alpha = manager.getRenderer().getFadeAlpha(alpha);
            int i = FastColor.ARGB32.colorFromFloat(alpha, 0f, 0f, 0f);
            guiGraphics.fill(RenderType.guiOverlay(), 0, 0,
                guiGraphics.guiWidth(), guiGraphics.guiHeight(), i);
        }
    }
}
