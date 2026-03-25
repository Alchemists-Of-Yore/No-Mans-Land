package com.farcr.nomansland.client;

import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import com.farcr.nomansland.common.networking.ServerboundMooseBeginJumpSequencePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class NMLMooseChargeAttackHandler {

    public static int chargeTicks;
    public static float chargeScale;

    public static boolean handleCustomChargeAttackLogic(LocalPlayer player, boolean wasJumping) {
        var vehicle = player.jumpableVehicle();
        if (!(vehicle instanceof Moose moose)) {
            chargeTicks = 0;
            chargeScale = 0;
            return false;
        }
        if (!vehicle.canJump() || vehicle.getJumpCooldown() > 0) {
            chargeTicks = 0;
            chargeScale = 0;
            return false;
        }

        if (chargeTicks < 0) {
            chargeTicks++;
            if (chargeTicks == 0) {
                chargeScale = 0.0F;
            }
        }

        boolean isJumping = player.input.jumping;
        if ((wasJumping && !isJumping) || chargeTicks >= 60) {
            chargeTicks = -20;
            int strength = Mth.floor(chargeScale * chargeScale * 100f);
            moose.onPlayerJump(strength);
            player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_RIDING_JUMP, strength));
        } else if (!wasJumping && isJumping) {
            chargeTicks = 0;
            chargeScale = 0.0F;
        } else if (wasJumping) {
            chargeTicks++;
            if (chargeTicks == 1) {
                PacketDistributor.sendToServer(new ServerboundMooseBeginJumpSequencePacket(moose.getUUID()));
            }
            if (chargeTicks <= 40) {
                chargeScale = (float) chargeTicks / 40f;
            } else if (chargeTicks >= 42) {
                chargeScale = 0.6F + 4.0F / (float) (chargeTicks - 41) * 0.1F;
            }
            chargeScale = chargeScale * chargeScale * chargeScale;
        }
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    public static boolean shouldMoveSlower(Moose moose) {
        var player = Minecraft.getInstance().player;
        if (moose.equals(player.getVehicle())) {
            return player.input.jumping && moose.canJump();
        }
        return false;
    }

    public static Optional<Float> replaceVanillaGuiValue(PlayerRideableJumping rideable) {
        if (rideable instanceof Moose) {
            return Optional.of(chargeScale);
        }
        return Optional.empty();
    }
}