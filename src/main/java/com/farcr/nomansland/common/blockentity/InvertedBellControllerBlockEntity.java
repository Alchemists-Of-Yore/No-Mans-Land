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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkPyramid;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class InvertedBellControllerBlockEntity extends BlockEntity {
    public static final TicketType<ChunkPos> BELL_TICKET = TicketType.create("nml:inverted_bell", Comparator.comparingLong(ChunkPos::toLong), 300);
    public static final int COOLDOWN = 100;

    public PositionState state = PositionState.DONT_SEARCH;

    private @Nullable ChunkPos targetArea;
    // slowly escalates chunk ticket level to spread generation out over time
    private int escalationTimer = 10;
    private int escalationValue = 0;

    public @Nullable BlockPos targetBell;
    public @Nullable Direction targetDir;

    public int ringCooldown = 0;

    private boolean beingDestroyed = false;

    public InvertedBellControllerBlockEntity(final BlockPos pos, final BlockState blockState) {
        super(NMLBlockEntities.INVERTED_BELL.get(), pos, blockState);
    }

    public void destroyBell() {
        // no cascading block updates
        if (this.beingDestroyed) {
            return;
        }
        this.beingDestroyed = true;

        final BlockPos.MutableBlockPos mutPos = new BlockPos.MutableBlockPos();
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                for (int y = -1; y < 2; y++) {
                    if (this.level.getBlockState(mutPos.setWithOffset(this.getBlockPos(), x, y, z)).is(NMLBlocks.INVERTED_BELL.block())) {
                        this.level.destroyBlock(mutPos, false);
                    }
                }
            }
        }
    }

    public void ring(final int direction) {
        if (this.getLevel() instanceof final ServerLevel serverLevel) {
//            if (this.state == PositionState.UNASSIGNED) {
//                final BellSanctuaryGrid grid = BellSanctuaryGridHandler.getGrid(serverLevel.getSeed());
//                final BellSanctuaryCell cell = grid.getCell(this.getBlockPos().getX(), this.getBlockPos().getZ());
//                if (cell != null) {
//                    this.targetArea = getLikelyOtherSanctuary(cell, this.getBlockPos());
//                    this.state = PositionState.CHUNK;
//                } else {
//                    NoMansLand.LOGGER.error("Inverted Bell at {} failed to find approximate pair region", this.getBlockPos());
//                    this.state = PositionState.DONT_SEARCH;
//                }
//            }

            if (this.state == PositionState.BLOCK_POS && this.targetBell != null) {
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

    public void link(InvertedBellControllerBlockEntity other) {
        this.targetBell = other.getBlockPos();
        this.targetDir = other.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        this.state = PositionState.BLOCK_POS;

        other.targetBell = this.getBlockPos();
        other.targetDir = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        other.state = PositionState.BLOCK_POS;
    }

    @Override
    public boolean triggerEvent(final int id, final int type) {
        if (id == 1) {
            this.ring(type - 1);
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public static void tick(final Level level, final BlockPos pos, final BlockState state, final InvertedBellControllerBlockEntity ibbe) {
        if (ibbe.ringCooldown > 0) {
            ibbe.ringCooldown--;
        }

        //TODO: change pair gathering to be on bell ring instead of tick; otherwise we'll have issues with chunkloading taking longer when this bell is generated.
        if (level instanceof final ServerLevel serverLevel) {
//            if (ibbe.state == PositionState.CHUNK) {
//                handleAwaitingTheSearch(ibbe, pos, serverLevel);
//            }

            if (ibbe.state == PositionState.UNASSIGNED) {
                final BellSanctuaryGrid grid = BellSanctuaryGridHandler.getGrid(serverLevel.getSeed());
                final BellSanctuaryCell cell = grid.getCell(pos.getX(), pos.getZ());
                if (cell != null) {
                    ibbe.targetArea = getLikelyOtherSanctuary(cell, pos);
                    ibbe.state = PositionState.CHUNK;
                } else {
                    NoMansLand.LOGGER.error("Inverted Bell at {} failed to find approximate pair region", pos);
                    ibbe.state = PositionState.DONT_SEARCH;
                }
            } else if (ibbe.state == PositionState.CHUNK) {
                handleAwaitingTheSearch(ibbe, pos, serverLevel);
            }
        }
    }

    @Nullable
    private static ChunkPos getLikelyOtherSanctuary(final BellSanctuaryCell cell, final BlockPos pos) {
        final ChunkPos currentPos = new ChunkPos(pos);

        final BellSanctuaryCell.SanctuaryPair pair = cell.getPair(currentPos);
        if (pair != null) {
            return pair.getOther(currentPos);
        }

        return null;
    }

    private static void handleAwaitingTheSearch(final InvertedBellControllerBlockEntity ibbe, final BlockPos pos, final ServerLevel serverLevel) {
        if (ibbe.escalationValue < ChunkPyramid.GENERATION_PYRAMID.steps().size()) {
            ibbe.escalationTimer++;
            if (ibbe.escalationTimer > 10) {
                serverLevel.getChunkSource().addRegionTicket(BELL_TICKET, ibbe.targetArea, 0, ibbe.targetArea);
                ibbe.escalationTimer = 0;
                ibbe.escalationValue++;
            }
        } else {
            final InvertedBellControllerBlockEntity otherIbbe = handleTheSearch(serverLevel, ibbe.targetArea);
            if (otherIbbe != null) {
                ibbe.link(otherIbbe);
            } else {
                NoMansLand.LOGGER.error("Inverted Bell at {} || {} failed to find pair around chunk {} || {}", pos, new ChunkPos(pos), ibbe.targetArea.getBlockAt(8, 0, 8), ibbe.targetArea);
                ibbe.state = PositionState.DONT_SEARCH;
            }
        }
    }

    private static @Nullable InvertedBellControllerBlockEntity handleTheSearch(final ServerLevel level, final ChunkPos target) {
        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                final LevelChunk chunk = level.getChunk(target.x + x, target.z + z);
                for (final BlockPos bePos : chunk.getBlockEntitiesPos()) {
                    if (chunk.getBlockEntity(bePos) instanceof final InvertedBellControllerBlockEntity ibbe) {
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
        /**
         * Default state when placed by a player, or fallback state if the linking process fails
         */
        DONT_SEARCH,
        /**
         * State when just placed by world gen. References its {@link BellSanctuaryCell} to get an approximate target position and switches to {@link PositionState#CHUNK}
         */
        UNASSIGNED,
        /**
         * State when slowly generating chunks at its target area. Once complete, searches for another block entity within those chunks and switches to {@link PositionState#BLOCK_POS}
         */
        CHUNK,
        /**
         * State when a link has been established. The bell can be interacted with and used to teleport
         */
        BLOCK_POS
    }
}
