package com.farcr.nomansland.common.entity.buddy;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.networking.ClientboundBuddyCrouchPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

public class BuddyBehavior extends Behavior<Buddy> {
    public BuddyBehavior() {
        super(Map.of());
    }

    // Enderman Code
    private static final double STARE_PRECISION = 0.5; // 0.025 for endermen
    public boolean staringAt(LivingEntity player, Buddy buddy) {
        Vec3 vec3 = player.getViewVector(1.0F).normalize();
        Vec3 vec31 = new Vec3(buddy.getX() - player.getX(), buddy.getEyeY() - player.getEyeY(), buddy.getZ() - player.getZ());
        double d0 = vec31.length();
        vec31 = vec31.normalize();
        double d1 = vec3.dot(vec31);
        boolean lookingAt = (d1 > 1.0 - STARE_PRECISION / d0 && player.hasLineOfSight(buddy));
//        if (!lookingAt)
//            forgetStaring(player);
        return lookingAt;
    }

    public void forgetStaring(LivingEntity entity) {
        stareTime.remove(entity);
        greetTime.remove(entity);
    }

    private Map<LivingEntity, Integer> stareTime = new HashMap<>();
    public boolean boredOfStaring(LivingEntity player) {
        stareTime.put(player, stareTime.getOrDefault(player, 0) + 1);
        return (stareTime.get(player) < 30) && (greetTime.getOrDefault(player, 0) <= 0);
    }

    private Map<LivingEntity, Integer> greetTime = new HashMap<>();
    private int crouchTimer = 0;
    public void mimicPlayerGreeting(LivingEntity player, Buddy buddy) {
        int curGreetTime = greetTime.getOrDefault(player, 0);
        if (player.isCrouching())
            greetTime.put(player, greetTime.getOrDefault(player, 0) + 1);
        else if (curGreetTime > 0) {
            greetTime.put(player, greetTime.get(player) + 1);
            if (curGreetTime >= 3) {
                crouchTimer++;
                if (crouchTimer % 8 == 2) {
                    PacketDistributor.sendToPlayersTrackingEntity(
                        buddy, new ClientboundBuddyCrouchPacket(buddy.getId()));
                }
                if (crouchTimer > 20)
                    forgetStaring(player);
            }
        }
    }

    @Override
    protected void start(ServerLevel level, Buddy buddy, long gameTime) {
        var brain = buddy.getBrain();
        Optional<List<Player>> playerList = brain.getMemory(MemoryModuleType.NEAREST_PLAYERS);
        if (playerList.isPresent()) {
            Optional<Player> playerFocus = playerList.get().stream().filter(
                player -> staringAt(player, buddy)
            ).max(Comparator.comparingInt((player) -> greetTime.getOrDefault(player, 0)));
            if (playerFocus.isPresent()) {
                mimicPlayerGreeting(playerFocus.get(), buddy);
                buddy.getLookControl().setLookAt(playerFocus.get().getEyePosition(gameTime));
                return;
            }
        }
        crouchTimer = 0;

        // Look back at players who are staring
        NearestVisibleLivingEntities entities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
            .orElse(NearestVisibleLivingEntities.empty());
        Optional<LivingEntity> entity = entities.findClosest(player -> staringAt(player, buddy) && !boredOfStaring(player));
        entity.ifPresentOrElse(livingEntity -> {
            buddy.getLookControl().setLookAt(livingEntity.getEyePosition(gameTime));
        }, stareTime::clear);
    }
}
