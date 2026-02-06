package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(BrewingStandScreen.class)
public abstract class BrewingStandScreenMixin {

    @Unique
    private static final int nml$slotSize = 16;

    @Unique
    private static boolean nml$isBandage(ItemStack stack) {
        return !stack.isEmpty() && stack.is(NMLItems.BANDAGE);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void nml$hideBottleIconForBandages(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        BrewingStandScreen screen = (BrewingStandScreen) (Object) this;
        int leftPos = (screen.width - screen.getXSize()) / 2;
        int topPos = (screen.height - screen.getYSize()) / 2;
        int slotColor = 0xFF8B8B8B;
        for (int i = 0; i < 3; i++) {
            Slot slot = screen.getMenu().getSlot(i);
            if (nml$isBandage(slot.getItem())) {
                int x = leftPos + slot.x;
                int y = topPos + slot.y;
                guiGraphics.fill(x, y, x + nml$slotSize, y + nml$slotSize, slotColor);
            }
        }
    }
}
