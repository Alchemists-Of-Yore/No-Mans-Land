package com.farcr.nomansland.common.block.moonlight;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry.DialoguePool;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.swing.*;
import java.util.List;

public class MoonlightBasinBlockEntity extends BlockEntity {

    /* Temp code i stole from myself */
    public List<? extends DialoguePool> getDialogueList(ResourceKey<Registry<DialoguePool>> resourceKey) throws IllegalStateException {
        RegistryAccess registryAccess = level.registryAccess();
        try {
            Registry<? extends DialoguePool> tarotCardRegistry = registryAccess.registryOrThrow(resourceKey);
            return tarotCardRegistry.stream().toList();
        } catch (IllegalStateException e) {
            throw new RuntimeException("Failed to load registry: " + e);
        }
    }

    public MoonlightBasinBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOONLIGHT_BASIN.get(), pos, blockState);
    }

    @Override
    public void onLoad() {
        List<? extends DialoguePool> pool = getDialogueList(NMLRegistries.OFFERING_DIALOGUE_KEY);
        NoMansLand.LOGGER.info("loading dialogue list: " + pool.toString());
    }
}
