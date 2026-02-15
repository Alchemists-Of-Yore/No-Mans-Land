package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.IntStream;

public class PotBlockEntity extends BlockEntity implements WorldlyContainer, RandomizableContainer {

    public PotVariant variant = null;
    public long wobbleStartedAtTick;
    public @Nullable DecoratedPotBlockEntity.WobbleStyle lastWobbleStyle;
    private final NonNullList<ItemStack> items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    protected @Nullable ResourceKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;

    public PotBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.POT.get(), pos, state);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        Optional.ofNullable(level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
            tag.putString("Variant", key.toString());
        });

        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, items, registries);
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Variant")) {
            variant = registries.lookupOrThrow(NMLRegistries.POT_VARIANT_KEY).get(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(tag.getString("Variant")))).orElseThrow().value();
        }

        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, items, registries);
        }
    }

    public ItemStack getPotAsItem() {
        ItemStack itemstack = getBlockState().getBlock().asItem().getDefaultInstance();
        itemstack.applyComponents(this.collectComponents());
        return itemstack;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        Optional.ofNullable(level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
            components.set(NMLDataComponents.POT_VARIANT, key);
        });

        if (this.lootTable != null) {
            components.set(DataComponents.CONTAINER_LOOT, new SeededContainerLoot(this.lootTable, this.lootTableSeed));
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        Optional.ofNullable(componentInput.get(NMLDataComponents.POT_VARIANT)).ifPresent(key -> {
            variant = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, key)).orElse(null);
        });

        SeededContainerLoot seededcontainerloot = componentInput.get(DataComponents.CONTAINER_LOOT);
        if (seededcontainerloot != null) {
            this.lootTable = seededcontainerloot.lootTable();
            this.lootTableSeed = seededcontainerloot.seed();
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Variant");

        tag.remove("LootTable");
        tag.remove("LootTableSeed");
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    public Direction getDirection() {
        return this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public void wobble(DecoratedPotBlockEntity.WobbleStyle style) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, style.ordinal());
        }
    }

    public boolean triggerEvent(int id, int type) {
        if (this.level != null && id == 1 && type >= 0 && type < DecoratedPotBlockEntity.WobbleStyle.values().length) {
            this.wobbleStartedAtTick = this.level.getGameTime();
            this.lastWobbleStyle = DecoratedPotBlockEntity.WobbleStyle.values()[type];
            return true;
        } else {
            return super.triggerEvent(id, type);
        }
    }

    @Override
    public int getContainerSize() {
        return 64;
    }

    @Override
    public boolean isEmpty() {
        this.unpackLootTable(null);

        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        this.unpackLootTable(null);

        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        this.unpackLootTable(null);

        for (int i = 63; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                return ContainerHelper.removeItem(items, i, amount);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        this.unpackLootTable(null);

        for (int i = 63; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                return ContainerHelper.takeItem(items, i);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.unpackLootTable(null);

        items.set(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    public boolean insert(ItemStack stack) {
        this.unpackLootTable(null);

        for (int i = 0; i < 64; i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack);
                return true;
            }
        }

        return false;
    }

    public ItemStack extract() {
        this.unpackLootTable(null);

        for (int i = 63; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack stack = items.get(i);
                items.set(i, ItemStack.EMPTY);
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return IntStream.range(0, 64).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return direction != Direction.DOWN;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return direction != Direction.UP;
    }

    public boolean isFull() {
        this.unpackLootTable(null);

        for (ItemStack stack : items) {
            if (stack.isEmpty()) return false;
        }
        return true;
    }

    public int getItemCount() {
        this.unpackLootTable(null);

        int count = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) count += stack.getCount();
        }
        return count;
    }

    @Nullable
    public ResourceKey<LootTable> getLootTable() {
        return this.lootTable;
    }

    public void setLootTable(@Nullable ResourceKey<LootTable> lootTable) {
        this.lootTable = lootTable;
    }

    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setLootTableSeed(long seed) {
        this.lootTableSeed = seed;
    }
}
