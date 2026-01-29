package com.farcr.nomansland.common.block.moonlight;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry.DialoguePool;
import com.farcr.nomansland.common.networking.ClientboundDialoguePacket;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
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

    public static void tick(Level level, BlockPos pos, BlockState state, MoonlightBasinBlockEntity blockEntity) {
        AABB aabb = new AABB(pos).inflate(3);
        for (ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, aabb))
            serverplayer.addEffect(new MobEffectInstance(NMLEffects.FRIENDSHIP, 30, 0, true, true));
    }

    public void sendDialogue(ResourceLocation dialogueLocation, ResourceKey<Registry<DialoguePool>> registryKey) {
        if (!this.level.isClientSide())
            PacketDistributor.sendToAllPlayers(new ClientboundDialoguePacket(dialogueLocation, registryKey.location()));
    }

    @Override
    public void onLoad() {
        sendDialogue(NoMansLand.location("passive_1"), NMLRegistries.PASSIVE_DIALOGUE_KEY);
    }
}
