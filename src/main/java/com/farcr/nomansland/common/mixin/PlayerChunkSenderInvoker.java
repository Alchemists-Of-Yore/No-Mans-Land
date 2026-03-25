package com.farcr.nomansland.common.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerChunkSender.class)
public interface PlayerChunkSenderInvoker {
    @Invoker
    static void invokeSendChunk(ServerGamePacketListenerImpl packetListener, ServerLevel level, LevelChunk chunk) {}

    @Accessor
    int getUnacknowledgedBatches();
    @Accessor
    void setUnacknowledgedBatches(int unacknowledgedBatches);
}
