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

public class InvertedBellBlockEntity extends BlockEntity {
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

    private @Nullable BlockPos controller;
    private boolean valid = true; // invalidates when destroyed to prevent cascading block updates

    public int ringCooldown = 0;

    public InvertedBellBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.INVERTED_BELL.get(), pos, blockState);
    }

    public boolean isController() {
        return this.getBlockState().getValue(InvertedBellBlock.PART) == InvertedBellBlock.CONTROLLER_PART;
    }

    public boolean canSurvive() {
        InvertedBellBlockEntity controller = this.getController();
        if (controller != null) {
            BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    for (int y = -1; y < 2; y++) {
                        mutPos.setWithOffset(controller.getBlockPos(), x, y, z);
                        if (!controller.level.getBlockState(mutPos).is(NMLBlocks.INVERTED_BELL.block())) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        return false;
    }

    public void destroyBell() {
        if (!this.valid) {
            return;
        }

        InvertedBellBlockEntity controller = this.getController();
        if (controller != null) {
            BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    for (int y = -1; y < 2; y++) {
                        if (controller.level.getBlockEntity(mutPos.setWithOffset(controller.getBlockPos(), x, y, z))
                                instanceof InvertedBellBlockEntity ibbe) {
                            ibbe.valid = false;
                        }
                    }
                }
            }

            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    for (int y = -1; y < 2; y++) {
                        if (controller.level.getBlockState(mutPos.setWithOffset(controller.getBlockPos(), x, y, z))
                                .is(NMLBlocks.INVERTED_BELL.block())) {
                            controller.level.destroyBlock(mutPos, false);
                        }
                    }
                }
            }
        }
    }

    public void ring(int direction, boolean teleport) {
        InvertedBellBlockEntity controller = this.getController();
        if (controller == null) {
            return;
        }

        if (controller.level instanceof ServerLevel serverLevel) {
            if (teleport && controller.state == PositionState.BLOCK_POS && controller.targetBell != null) {
                InvertedBellServerHandler.get(serverLevel).beginTeleport(serverLevel,
                        controller.getBlockPos(), controller.getBlockState().getValue(InvertedBellBlock.HORIZONTAL_FACING),
                        controller.targetBell, controller.targetDir
                );
            }
            controller.ringCooldown = COOLDOWN;
        } else {
            InvertedBellClientHandler.instance.onHit(direction);
        }
    }

    public @Nullable InvertedBellBlockEntity getController() {
        if (this.controller != null) {
            BlockEntity be = this.level.getBlockEntity(this.controller);
            if (be instanceof InvertedBellBlockEntity ibbe && ibbe.isController()) {
                return ibbe;
            }
        }

        BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    mutPos.setWithOffset(this.getBlockPos(), x, y, z);
                    BlockEntity be = this.level.getBlockEntity(mutPos);
                    if (be instanceof InvertedBellBlockEntity ibbe && ibbe.isController()) {
                        this.controller = mutPos;
                        return ibbe;
                    }
                }
            }
        }

        return null;
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

    public static void tick(Level level, BlockPos pos, BlockState state, InvertedBellBlockEntity ibbe) {
        if (ibbe.ringCooldown > 0) {
            ibbe.ringCooldown--;
        }
        if (level instanceof ServerLevel serverLevel) {
            BellSanctuaryGrid grid = BellSanctuaryGridHandler.getGrid(serverLevel.getSeed());
            BellSanctuaryCell cell = grid.getCell(pos.getX(), pos.getZ());
            if (cell != null) {
                switch (ibbe.state) {
                    case DONT_SEARCH -> {}
                    case UNASSIGNED -> {
                        ibbe.targetArea = getLikelyOtherSanctuary(cell, pos);
                        ibbe.state = PositionState.CHUNK;
                    }
                    case CHUNK -> {
                        handleAwaitingTheSearch(ibbe, pos, serverLevel);
                    }
                    case BLOCK_POS -> {}
                }
            } else if (ibbe.state == PositionState.UNASSIGNED) {
                NoMansLand.LOGGER.error("Inverted Bell at {} failed to find approximate pair region", pos);
                ibbe.state = PositionState.DONT_SEARCH;
            }
        }
    }

    private static ChunkPos getLikelyOtherSanctuary(BellSanctuaryCell cell, BlockPos pos) {
        double dd1 = cell.getFirstBellSanctuaryPos().distanceSquared(new ChunkPos(pos));
        double dd2 = cell.getSecondBellSanctuaryPos().distanceSquared(new ChunkPos(pos));
        if (dd1 > dd2) {
            return cell.getFirstBellSanctuaryPos();
        } else {
            return cell.getSecondBellSanctuaryPos();
        }
    }

    private static CompletableFuture<List<ChunkResult<ChunkAccess>>> tryLoadOtherSanctuary(ServerLevel level, ChunkPos other) {
        List<CompletableFuture<ChunkResult<ChunkAccess>>> futures = new ArrayList<>(9);
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                futures.add(level.getChunkSource().getChunkFuture(other.x+x, other.z+z, ChunkStatus.FULL, true));
            }
        }
        // black magic https://www.baeldung.com/java-completablefuture-list-convert
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    private static void handleAwaitingTheSearch(InvertedBellBlockEntity ibbe, BlockPos pos, ServerLevel serverLevel) {
        if (ibbe.targetAreaFuture != null) {
            if (ibbe.targetAreaFuture.isDone()) {
                try {
                    InvertedBellBlockEntity otherIbbe = handleTheSearch(ibbe.targetAreaFuture);
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
                } catch (ExecutionException | InterruptedException e) {
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
                    ChunkStep step = ChunkPyramid.GENERATION_PYRAMID.steps().get(ibbe.escalationValue);
                    int dist = ChunkLevel.byStatus(step.targetStatus());
                    serverLevel.getChunkSource().addRegionTicket(BELL_TICKET, ibbe.targetArea, dist, ibbe.targetArea);
                    ibbe.escalationTimer = 0;
                    ibbe.escalationValue++;
                }
            } else {
                ibbe.targetAreaFuture = tryLoadOtherSanctuary(serverLevel, ibbe.targetArea);
            }
        }
    }

    private static @Nullable InvertedBellBlockEntity handleTheSearch(CompletableFuture<List<ChunkResult<ChunkAccess>>> targetAreaFuture) throws ExecutionException, InterruptedException {
        List<ChunkResult<ChunkAccess>> chunks = targetAreaFuture.get();
        for (ChunkResult<ChunkAccess> chunk : chunks) {
            if (chunk.isSuccess()) {
                ChunkAccess access = chunk.orElseThrow(AssertionError::new);
                for (BlockPos bePos : access.getBlockEntitiesPos()) {
                    if (access.getBlockEntity(bePos) instanceof InvertedBellBlockEntity ibbe && ibbe.isController()) {
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
        int state = tag.getInt("State");
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
