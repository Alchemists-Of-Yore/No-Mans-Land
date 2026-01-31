package com.farcr.nomansland.common.block.moonlight;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry.DialoguePool;
import com.farcr.nomansland.common.networking.ClientboundDialoguePacket;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class MoonlightBasinBlockEntity extends BlockEntity {

    public MoonlightBasinBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOONLIGHT_BASIN.get(), pos, blockState);
    }

    private boolean hasResetValues = false;
    private boolean moonAwake = false;

    public static void wakeUpMoon() {}

    /* Runs at nighttime */
    private void resetValues() {

        // Flag values as reset
        hasResetValues = true;
        moonAwake = true; //placeholder test
        setChanged();
    }

    public static boolean isNightTime(Level level) {
        return (level.getSkyDarken() >= 10);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.moonAwake = tag.getBoolean("MoonAwake");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("MoonAwake", moonAwake);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MoonlightBasinBlockEntity blockEntity) {
        if (!level.isClientSide()) {
            if (isNightTime(level)) {
                if (!blockEntity.hasResetValues)
                    blockEntity.resetValues();
            } else
                blockEntity.hasResetValues = false;

            // Give Players in vicinity Friendship effect if they don't have it
            AABB aabb = new AABB(pos).inflate(5);
            for (ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, aabb))
                serverplayer.addEffect(new MobEffectInstance(NMLEffects.FRIENDSHIP, 30, 0, true, false));
        }
//        NoMansLand.LOGGER.info("moon awake: " + blockEntity.moonAwake);
    }

    public void sendDialogue(ResourceLocation dialogueLocation, ResourceKey<Registry<DialoguePool>> registryKey) {
        if (!this.level.isClientSide())
            PacketDistributor.sendToAllPlayers(new ClientboundDialoguePacket(dialogueLocation, registryKey.location()));
    }

    @Override
    public void onLoad() {
//        sendDialogue(NoMansLand.location("passive_1"), NMLRegistries.PASSIVE_DIALOGUE_KEY);
    }
}
