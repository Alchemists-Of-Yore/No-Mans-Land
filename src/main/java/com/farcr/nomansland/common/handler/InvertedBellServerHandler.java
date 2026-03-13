package com.farcr.nomansland.common.handler;

import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.mixin.PlayerChunkSenderInvoker;
import com.farcr.nomansland.common.networking.ClientboundDistantChunkPacket;
import com.farcr.nomansland.common.networking.ClientboundInvertedBellPacket;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class InvertedBellServerHandler extends SavedData {
    // difference in timing is needed due to a bug with simultaneous teleporting into loaded chunks :p
    public static final int TELEPORT_ENTITY_TIME = 25;
    public static final int TELEPORT_PLAYER_TIME = 35;
    public static final int FAILURE_NAUSEA_DURATION = 200;
    public static final double RANGE_SQUARED = 16*16;

    private final List<ActiveTeleport> teleports = new ArrayList<>();

    public void beginTeleport(ServerLevel level, BlockPos fromPos, Direction fromDir, BlockPos toPos, Direction toDir) {
        this.teleports.add(new ActiveTeleport(level, fromPos, fromDir, toPos, toDir));
    }

    public static boolean canTeleport(Entity entity, Vec3 from) {
        return entity instanceof LivingEntity &&
                !entity.getType().is(NMLTags.INVERTED_BELL_UNAFFECTED) &&
                entity.distanceToSqr(from) < InvertedBellServerHandler.RANGE_SQUARED &&
                entity.level().isBlockInLine(
                        new ClipBlockStateContext(entity.getEyePosition(), from, state -> state.is(BlockTags.OCCLUDES_VIBRATION_SIGNALS))
                ).getType() != HitResult.Type.BLOCK;
    }

    public void tick(ServerLevel level) {
        Iterator<ActiveTeleport> it = this.teleports.iterator();
        while (it.hasNext()) {
            ActiveTeleport entry = it.next();
            if (entry.chunkFutureIsFailure(level)) {
                entry.stop(level);
                it.remove();
            } else if (entry.tick(level)) {
                it.remove();
            }
            this.setDirty();
        }
    }

    public static InvertedBellServerHandler get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<InvertedBellServerHandler>(
                InvertedBellServerHandler::new, InvertedBellServerHandler::load
        ), "inverted_bell");
    }

    public static InvertedBellServerHandler load(CompoundTag compoundTag, HolderLookup.Provider registries) {
        return new InvertedBellServerHandler();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag teleportsTag = new CompoundTag();
        return teleportsTag;
    }

    public static class ActiveTeleport {
        private int timer = 0;
        private List<Entity> teleportingEntities;
        private List<ServerPlayer> teleportingPlayers;
        private BlockPos fromPos;
        private Direction fromDir;
        private BlockPos toPos;
        private Direction toDir;
        private @Nullable CompletableFuture<ChunkResult<ChunkAccess>> chunkFuture;

        private ActiveTeleport(ServerLevel level, BlockPos fromPos, Direction fromDir, BlockPos toPos, Direction toDir) {
            this.teleportingEntities = level.getEntities(null, new AABB(fromPos).inflate(16)).stream()
                    .filter(e -> InvertedBellServerHandler.canTeleport(e, fromPos.getCenter())).collect(Collectors.toList());
            this.teleportingPlayers = new ArrayList<>();
            Iterator<Entity> it = this.teleportingEntities.iterator();
            while (it.hasNext()) {
                if (it.next() instanceof ServerPlayer serverPlayer) {
                    this.teleportingPlayers.add(serverPlayer);
                    it.remove();
                }
            }

            this.fromPos = fromPos;
            this.fromDir = fromDir;
            this.toPos = toPos;
            this.toDir = toDir;

            ChunkPos fromChunk = new ChunkPos(toPos);
            level.getChunkSource().addRegionTicket(InvertedBellBlockEntity.BELL_TICKET, fromChunk, 0, fromChunk);
            ChunkPos toChunk = new ChunkPos(toPos);
            level.getChunkSource().addRegionTicket(InvertedBellBlockEntity.BELL_TICKET, toChunk, 0, toChunk);

            this.teleportingEntities.forEach(e -> {
                if (e instanceof LivingEntityExtension extension) {
                    extension.nml$beginBellParalysis();
                }
            });
            this.teleportingPlayers.forEach(e -> PacketDistributor.sendToPlayer(e, ClientboundInvertedBellPacket.FADE_IN));
        }

        public void stop(ServerLevel level) {
            if (this.chunkFuture != null) {
                this.chunkFuture.cancel(true);
            }
            ChunkPos centerChunk = new ChunkPos(this.toPos);
        }

        public boolean tick(ServerLevel level) {
            this.timer++;
            if (this.timer == TELEPORT_ENTITY_TIME) {
                this.teleportEntities(level);
            }
            if (this.timer == TELEPORT_PLAYER_TIME) {
                this.teleportPlayers(level);
            }
            return this.timer > TELEPORT_PLAYER_TIME;
        }

        public boolean chunkFutureIsFailure(ServerLevel level) {
            if (this.chunkFuture == null || !this.chunkFuture.isDone()) {
                return false;
            }
            try {
                ChunkResult<ChunkAccess> result = this.chunkFuture.get();
                this.chunkFuture = null;
                ChunkAccess access = result.orElseThrow(() -> new RuntimeException(result.getError()));
                if (!(access.getBlockEntity(this.toPos) instanceof InvertedBellBlockEntity ibbe) || !ibbe.targetBell.equals(this.fromPos)) {
                    return true;
                }
                if (access instanceof LevelChunk levelChunk) {
                    ClientboundDistantChunkPacket loadPacket = new ClientboundDistantChunkPacket(access.getPos().x, access.getPos().z);
                    ClientboundChunkBatchFinishedPacket chunkFinished = new ClientboundChunkBatchFinishedPacket(1);
                    this.teleportingPlayers.forEach(p -> {
                        PacketDistributor.sendToPlayer(p, loadPacket);
                        p.connection.send(ClientboundChunkBatchStartPacket.INSTANCE);
                        ((PlayerChunkSenderInvoker)p.connection.chunkSender).setUnacknowledgedBatches(
                                ((PlayerChunkSenderInvoker)p.connection.chunkSender).getUnacknowledgedBatches() + 1
                        );
                        PlayerChunkSenderInvoker.invokeSendChunk(p.connection, level, levelChunk);
                        p.connection.send(chunkFinished);
                    });
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }



        public void teleportEntities(ServerLevel level) {
            for (Entity entity : this.teleportingEntities) {
                if (entity.distanceToSqr(this.fromPos.getCenter()) < InvertedBellServerHandler.RANGE_SQUARED) {
                    if (entity.getType().is(NMLTags.INVERTED_BELL_REPULSED)) {
                        if (entity instanceof LivingEntityExtension extension) {
                            extension.nml$skipDroppingDeathLoot();
                        }
                        entity.kill();
                    } else {
                        this.doTeleportEntity(entity, level);
                        if (level.getBlockEntity(this.fromPos) instanceof InvertedBellBlockEntity fromIbbe &&
                                level.getBlockEntity(this.toPos) instanceof InvertedBellBlockEntity toIbbe) {
                            toIbbe.ringCooldown = fromIbbe.ringCooldown;
                        }
                    }
                }
            }
        }

        public void teleportPlayers(ServerLevel level) {
            for (ServerPlayer serverPlayer : this.teleportingPlayers) {
                if (!this.doTeleportEntity(serverPlayer, level)) {
                    PacketDistributor.sendToPlayer(serverPlayer, ClientboundInvertedBellPacket.FADE_OUT_PAINFUL);
                } else {
                    PacketDistributor.sendToPlayer(serverPlayer, ClientboundInvertedBellPacket.FADE_OUT);
                }
            }
        }

        // return true if success
        private boolean doTeleportEntity(Entity entity, ServerLevel level) {
            Vec3 diff = entity.position().subtract(this.fromPos.getCenter());
            float dYRot = this.fromDir.toYRot() - this.toDir.toYRot();
            diff = diff.yRot((float)(dYRot / 180 * Math.PI));
            Vec3 newPos = this.toPos.getCenter().add(diff);

            if (entityAtPositionIsColliding(entity, newPos, level)) {
                entity.hurt(level.damageSources().cramming(), 10);
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, FAILURE_NAUSEA_DURATION));
                }
                return false;
            } else {
                entity.teleportTo(level, newPos.x, newPos.y, newPos.z,
                        EnumSet.noneOf(RelativeMovement.class),
                        entity.getYRot() - dYRot, entity.getXRot());
                return true;
            }
        }

        public static boolean entityAtPositionIsColliding(Entity entity, Vec3 pos, ServerLevel level) {
            AABB bb = entity.getBoundingBox();
            bb = bb.move(pos.subtract(bb.getBottomCenter())).deflate(1E-2);
            BlockCollisions<VoxelShape> collisions = new BlockCollisions<>(level, entity, bb, true, (mutPos, shape) -> shape);
            return collisions.hasNext();
        }
    }
}
