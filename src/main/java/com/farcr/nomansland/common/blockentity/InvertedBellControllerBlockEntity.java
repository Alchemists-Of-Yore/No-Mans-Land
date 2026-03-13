package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryCell;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGrid;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkStep;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class InvertedBellControllerBlockEntity extends BlockEntity {
    public static final TicketType<ChunkPos> BELL_TICKET = TicketType.create("nml:inverted_bell", Comparator.comparingLong(ChunkPos::toLong), 300);
    public static final int COOLDOWN = 100;

    public PositionState state = PositionState.DONT_SEARCH;
    private @Nullable ChunkPos targetArea;
    // slowly escalates chunk ticket level to spread generation out over time
    private int escalationTimer = 20;
    private int escalationValue = 0;
    private @Nullable CompletableFuture<List<ChunkResult<ChunkAccess>>> targetAreaFuture;
    public @Nullable BlockPos targetBell;
    public @Nullable Direction targetDir;

    public int ringCooldown = 0;

    public InvertedBellControllerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(NMLBlockEntities.INVERTED_BELL.get(), pos, blockState);
    }

    public void destroyBell() {
        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    if (this.level.getBlockState(mutPos.setWithOffset(this.getBlockPos(), x, y, z))
                            .is(NMLBlocks.INVERTED_BELL.block())) {

                        this.level.destroyBlock(mutPos, false);
                    }
                }
            }
        }
    }

    public void ring(final int direction, final boolean teleport) {
        if (this.getLevel() instanceof final ServerLevel serverLevel) {
            if (teleport && this.state == PositionState.BLOCK_POS && this.targetBell != null) {
                InvertedBellServerHandler.get(serverLevel).beginTeleport(serverLevel,
                        this.getBlockPos(), this.getBlockState().getValue(InvertedBellBlock.HORIZONTAL_FACING),
                        this.targetBell, this.targetDir
                );
            }
            this.ringCooldown = COOLDOWN;
        } else {
            InvertedBellClientHandler.instance.onHit(direction);
        }
    }

    @Override
    public boolean triggerEvent(final int id, final int type) {
        if (id == 1) {
            this.ring(type - 1, true);
            return true;
        } else if (id == 2) {
            this.ring(type - 1, false);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public static void tick(final Level level, final BlockPos pos, final BlockState state, final InvertedBellControllerBlockEntity ibbe) {
        if (ibbe.ringCooldown > 0) {
            ibbe.ringCooldown--;
        }

        if (level instanceof final ServerLevel serverLevel) {
            final BellSanctuaryGrid grid = BellSanctuaryGridHandler.getGrid(serverLevel.getSeed());
            final BellSanctuaryCell cell = grid.getCell(pos.getX(), pos.getZ());
            if (cell != null) {
                switch (ibbe.state) {
                    case UNASSIGNED -> {
                        ibbe.targetArea = getLikelyOtherSanctuary(cell, pos);
                        ibbe.state = PositionState.CHUNK;
                    }
                    case CHUNK -> handleAwaitingTheSearch(ibbe, pos, serverLevel);
                }

            } else if (ibbe.state == PositionState.UNASSIGNED) {
                NoMansLand.LOGGER.error("Inverted Bell at {} failed to find approximate pair region", pos);
                ibbe.state = PositionState.DONT_SEARCH;
            }
        }
    }

    private static ChunkPos getLikelyOtherSanctuary(final BellSanctuaryCell cell, final BlockPos pos) {
        final double dd1 = cell.getFirstBellSanctuaryPos().distanceSquared(new ChunkPos(pos));
        final double dd2 = cell.getSecondBellSanctuaryPos().distanceSquared(new ChunkPos(pos));
        if (dd1 > dd2) {
            return cell.getFirstBellSanctuaryPos();
        } else {
            return cell.getSecondBellSanctuaryPos();
        }
    }

    private static CompletableFuture<List<ChunkResult<ChunkAccess>>> tryLoadOtherSanctuary(final ServerLevel level, final ChunkPos other) {
        final List<CompletableFuture<ChunkResult<ChunkAccess>>> futures = new ArrayList<>(9);
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                futures.add(level.getChunkSource().getChunkFuture(other.x + x, other.z + z, ChunkStatus.FULL, true));
            }
        }
        // black magic https://www.baeldung.com/java-completablefuture-list-convert
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    private static void handleAwaitingTheSearch(final InvertedBellControllerBlockEntity ibbe, final BlockPos pos, final ServerLevel serverLevel) {
        if (ibbe.targetAreaFuture != null) {
            if (ibbe.targetAreaFuture.isDone()) {
                try {
                    final InvertedBellControllerBlockEntity otherIbbe = handleTheSearch(ibbe.targetAreaFuture);
                    if (otherIbbe != null) {
                        ibbe.targetBell = otherIbbe.getBlockPos();
                        ibbe.targetDir = otherIbbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                        ibbe.state = PositionState.BLOCK_POS;

                        otherIbbe.targetBell = ibbe.getBlockPos();
                        otherIbbe.targetDir = ibbe.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
                        otherIbbe.state = PositionState.BLOCK_POS;
                    } else {
                        NoMansLand.LOGGER.error("Inverted Bell at {} mundanely failed to find pair around {}", pos, ibbe.targetArea);
                        ibbe.state = PositionState.DONT_SEARCH;
                    }
                } catch (final ExecutionException | InterruptedException e) {
                    NoMansLand.LOGGER.error("Inverted Bell at {} exceptionally failed to find pair around {}\n{}", pos, ibbe.targetArea, e);
                    ibbe.state = PositionState.DONT_SEARCH;
                }
                ibbe.targetAreaFuture = null;
            } else if (ibbe.targetAreaFuture.isCancelled() || ibbe.targetAreaFuture.isCompletedExceptionally()) {
                NoMansLand.LOGGER.error("Inverted Bell at {} exceptionally failed to find pair around {}", pos, ibbe.targetArea);
                ibbe.state = PositionState.DONT_SEARCH;
                ibbe.targetAreaFuture = null;
            }
        } else {
            if (ibbe.escalationValue < ChunkPyramid.GENERATION_PYRAMID.steps().size()) {
                ibbe.escalationTimer++;
                if (ibbe.escalationTimer > 10) {
                    final ChunkStep step = ChunkPyramid.GENERATION_PYRAMID.steps().get(ibbe.escalationValue);
                    final int dist = ChunkLevel.byStatus(step.targetStatus());
                    serverLevel.getChunkSource().addRegionTicket(BELL_TICKET, ibbe.targetArea, dist, ibbe.targetArea);
                    ibbe.escalationTimer = 0;
                    ibbe.escalationValue++;
                }
            } else {
                ibbe.targetAreaFuture = tryLoadOtherSanctuary(serverLevel, ibbe.targetArea);
            }
        }
    }

    private static @Nullable InvertedBellControllerBlockEntity handleTheSearch(final CompletableFuture<List<ChunkResult<ChunkAccess>>> targetAreaFuture) throws ExecutionException, InterruptedException {
        final List<ChunkResult<ChunkAccess>> chunks = targetAreaFuture.get();
        for (final ChunkResult<ChunkAccess> chunk : chunks) {
            if (chunk.isSuccess()) {
                final ChunkAccess access = chunk.orElseThrow(AssertionError::new);
                for (final BlockPos bePos : access.getBlockEntitiesPos()) {
                    if (access.getBlockEntity(bePos) instanceof final InvertedBellControllerBlockEntity ibbe) {
                        return ibbe;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void saveAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("State", this.state.ordinal());

        switch (this.state) {
            case CHUNK -> {
                tag.putInt("chunkX", this.targetArea.x);
                tag.putInt("chunkZ", this.targetArea.z);
            }
            case BLOCK_POS -> {
                tag.putInt("targetX", this.targetBell.getX());
                tag.putInt("targetY", this.targetBell.getY());
                tag.putInt("targetZ", this.targetBell.getZ());
                tag.putInt("targetOrientation", this.targetDir.get2DDataValue());
            }
        }
    }

    @Override
    protected void loadAdditional(final CompoundTag tag, final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        final int state = tag.getInt("State");
        if (state >= 0 && state < PositionState.values().length) {
            this.state = PositionState.values()[state];
        }
        switch (this.state) {
            case CHUNK -> {
                this.targetArea = new ChunkPos(
                        tag.getInt("chunkX"),
                        tag.getInt("chunkZ")
                );
            }
            case BLOCK_POS -> {
                this.targetBell = new BlockPos(
                        tag.getInt("targetX"),
                        tag.getInt("targetY"),
                        tag.getInt("targetZ")
                );
                this.targetDir = Direction.from2DDataValue(tag.getInt("targetOrientation"));
            }
        }
    }

    public enum PositionState {
        DONT_SEARCH, // default do nothing, or unassigned failed to find other sanctuary
        UNASSIGNED, // immediately when placed by world gen
        CHUNK, // gathered chunk position from sanctuary cell data
        BLOCK_POS // gathered exact position from POI manager after other chunks generate
    }
}
