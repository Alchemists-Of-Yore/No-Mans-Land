package com.farcr.nomansland.common.dreams.dreamlevel;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.PacketListener;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DreamingPlayer extends FakePlayer {
    // tethered player for client rendering / tricking clients into thinking player is x
    private final ServerPlayer tetheredPlayer;
    public DreamingPlayer(ServerLevel level, ServerPlayer player) {
        super(
            level,
            new GameProfile(
                UUID.nameUUIDFromBytes(("DummyDreamingPlayer:" + player.getGameProfile().getName())
                    .getBytes(StandardCharsets.UTF_8)),
                player.getGameProfile().getName()
            )
        );
        tetheredPlayer = player;
    }

//    @Override
//    public void restoreFrom(ServerPlayer serverPlayer, boolean keepEverything) {
//        super.restoreFrom(serverPlayer, keepEverything);
//
//        // Additional restores
//        setPos(serverPlayer.position());
//        setXRot(serverPlayer.getXRot());
//        setYRot(serverPlayer.getYRot());
//        setPose(serverPlayer.getPose());
//    }

    @Override
    public UUID getUUID() {
        return super.getUUID();
    }

    @Override
    public void tick() {
        // ensure tethered player is not on same server otherwise DIE!!!!
//        if (tetheredPlayer.serverLevel().equals(this.serverLevel()))
//            this.remove(RemovalReason.DISCARDED);

        // resume
        super.tick();
    }
}
