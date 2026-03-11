package com.farcr.nomansland.common.handler;

import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.farcr.nomansland.common.networking.InvertedBellPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class InvertedBellServerHandler extends SavedData {
    public static final int TELEPORT_WINDUP_TIME = 30;
    public static final int FAILURE_NAUSEA_DURATION = 200;

    private final List<ActiveTeleport> teleports = new ArrayList<>();

    public void beginTeleport(ServerLevel level, BlockPos from, BlockPos to) {
        this.teleports.add(new ActiveTeleport(level, from, to));
    }

    public void tick(ServerLevel level) {
        Iterator<ActiveTeleport> it = this.teleports.iterator();
        while (it.hasNext()) {
            ActiveTeleport entry = it.next();
            ChunkPos centerChunk = new ChunkPos(entry.to);
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    ChunkPos offChunk = new ChunkPos(centerChunk.x+x, centerChunk.z+z);
                    level.getChunkSource().removeRegionTicket(TicketType.FORCED, offChunk, 2, offChunk);
                }
            }
            if (entry.chunkFutureIsFailure()) {
                entry.stop(level);
                it.remove();
            } else if (entry.tick()) {
                entry.stop(level);
                entry.doTeleport(level);
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
        private List<Entity> teleporting;
        private BlockPos from;
        private BlockPos to;
        private @Nullable CompletableFuture<ChunkResult<ChunkAccess>> chunkFuture;

        private ActiveTeleport(ServerLevel level, BlockPos from, BlockPos to) {
            this.teleporting = level.getEntities(null, new AABB(from).inflate(16)).stream()
                    .filter(e -> e.distanceToSqr(from.getCenter()) < 16*16).toList();
            this.from = from;
            this.to = to;

            ChunkPos toChunk = new ChunkPos(to);
            level.getChunkSource().addRegionTicket(TicketType.FORCED, toChunk, 0, toChunk);
            this.chunkFuture = level.getChunkSource().getChunkFuture(toChunk.x, toChunk.z, ChunkStatus.FULL, true);

            this.teleporting.forEach(e -> {
                if (e instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, InvertedBellPacket.FADE_IN);
                }
            });
        }

        public void stop(ServerLevel level) {
            if (this.chunkFuture != null) {
                this.chunkFuture.cancel(true);
            }
            ChunkPos centerChunk = new ChunkPos(this.to);
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    ChunkPos offChunk = new ChunkPos(centerChunk.x+x, centerChunk.z+z);
                    level.getChunkSource().removeRegionTicket(TicketType.FORCED, offChunk, 2, offChunk);
                }
            }
        }

        public boolean tick() {
            this.timer++;
            return this.timer > TELEPORT_WINDUP_TIME && this.chunkFuture == null;
        }

        // return true if chunk future is a failure (targeting bell that does not exist)
        public boolean chunkFutureIsFailure() {
            if (this.chunkFuture == null || !this.chunkFuture.isDone()) {
                return false;
            }
            try {
                ChunkResult<ChunkAccess> result = this.chunkFuture.get();
                this.chunkFuture = null;
                ChunkAccess access = result.orElseThrow(() -> new RuntimeException(result.getError()));
                if (!(access.getBlockEntity(this.to) instanceof InvertedBellBlockEntity ibbe) || !ibbe.targetBell.equals(this.from)) {
                    return true;
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return false;
        }

        public void doTeleport(ServerLevel level) {
            // idk how forceloading works :(
            ChunkPos centerChunk = new ChunkPos(this.to);
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    level.getChunkSource().getChunk(centerChunk.x+x, centerChunk.z+z, ChunkStatus.FULL, true);
                }
            }
            for (Entity entity : this.teleporting) {
                if (entity.distanceToSqr(this.from.getCenter()) < 16*16) {
                    Vec3 diff = entity.position().subtract(this.from.getCenter());
                    Vec3 newPos = this.to.getCenter().add(diff);

                    if (entityAtPositionIsColliding(entity, newPos, level)) {
                        entity.hurt(level.damageSources().cramming(), 4);
                        if (entity instanceof LivingEntity livingEntity) {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, FAILURE_NAUSEA_DURATION));
                        }
                        if (entity instanceof ServerPlayer serverPlayer) {
                            PacketDistributor.sendToPlayer(serverPlayer, InvertedBellPacket.FADE_OUT_PAINFUL);
                        }
                    } else {
                        entity.teleportTo(newPos.x, newPos.y, newPos.z);
                        if (entity instanceof ServerPlayer serverPlayer) {
                            PacketDistributor.sendToPlayer(serverPlayer, InvertedBellPacket.FADE_OUT);
                        }
                    }
                }
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
