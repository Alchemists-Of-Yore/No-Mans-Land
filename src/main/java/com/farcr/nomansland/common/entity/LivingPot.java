package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LivingPot extends PathfinderMob {
    public BlockState blockState;
    public PotVariant variant;
    protected @Nullable ResourceKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;

    public LivingPot(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.blockState = NMLBlocks.ANCIENT_POT.get().defaultBlockState();
    }

    public LivingPot(Level level, double x, double y, double z, BlockState state) {
        this(NMLEntities.LIVING_POT.get(), level);
        this.blockState = state;
        this.setPos(x, y, z);
        this.setDeltaMovement(Vec3.ZERO);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        ItemStack stack = unpackLootTable(damageSource.getEntity() instanceof Player player ? player : null);
        spawnAtLocation(stack);
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        this.tryLoadLootTable(tag);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        Optional.ofNullable(level().registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
            tag.putString("Variant", key.toString());
        });

        this.trySaveLootTable(tag);
    }

    public boolean tryLoadLootTable(CompoundTag tag) {
        if (tag.contains("LootTable", 8)) {
            this.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(tag.getString("LootTable"))));
            if (tag.contains("LootTableSeed", 4)) {
                this.setLootTableSeed(tag.getLong("LootTableSeed"));
            } else {
                this.setLootTableSeed(0L);
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean trySaveLootTable(CompoundTag tag) {
        ResourceKey<LootTable> resourcekey = this.getLootTable();
        if (resourcekey == null) {
            return false;
        } else {
            tag.putString("LootTable", resourcekey.location().toString());
            long i = this.getLootTableSeed();
            if (i != 0L) {
                tag.putLong("LootTableSeed", i);
            }

            return true;
        }
    }

    public ItemStack unpackLootTable(@javax.annotation.Nullable Player player) {
        ResourceKey<LootTable> resourcekey = this.getLootTable();
        if (resourcekey != null && level() != null && level().getServer() != null) {
            LootTable loottable = level().getServer().reloadableRegistries().getLootTable(resourcekey);
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, resourcekey);
            }

            this.setLootTable(null);
            LootParams.Builder lootparams$builder = (new LootParams.Builder((ServerLevel)level())).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPosition()));
            if (player != null) {
                lootparams$builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }

            Container container = new ContainerSingleItem.BlockContainerSingleItem() {
                ItemStack item;

                @Override
                public BlockEntity getContainerBlockEntity() {
                    return null;
                }

                @Override
                public ItemStack getTheItem() {
                    return this.item;
                }

                @Override
                public void setTheItem(ItemStack itemStack) {
                    this.item = itemStack;
                }

                @Override
                public void setChanged() {

                }
            };

            loottable.fill(container, lootparams$builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
            return container.getItem(0);
        }

        return ItemStack.EMPTY;
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    public void setLootTableSeed(long lootTableSeed) {
        this.lootTableSeed = lootTableSeed;
    }
}
