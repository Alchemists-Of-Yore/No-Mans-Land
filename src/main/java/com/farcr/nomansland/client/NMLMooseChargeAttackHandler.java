package com.farcr.nomansland.client;

import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import com.farcr.nomansland.common.networking.ServerboundMooseBeginJumpSequencePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class NMLMooseChargeAttackHandler {

    public static void tryInitiate(LocalPlayer player, boolean wasJumping) {
        var vehicle = player.jumpableVehicle();
        if (!(vehicle instanceof Moose moose)) {
            return;
        }
        if (!vehicle.canJump()) {
            return;
        }
        if (!wasJumping && player.input.jumping) {
            PacketDistributor.sendToServer(new ServerboundMooseBeginJumpSequencePacket(moose.getUUID()));
        }
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean shouldMoveSlower(Moose moose) {
        var player = Minecraft.getInstance().player;
        if (moose.equals(player.getVehicle())) {
            return player.input.jumping && moose.canJump();
        }
        return false;
    }
}
