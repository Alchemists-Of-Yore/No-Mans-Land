package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.pots.PotModifier;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.block.pots.PotionTable;
import com.farcr.nomansland.common.block.pots.SeededPotionTable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.SeededContainerLoot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PotBlockEntity extends BlockEntity implements RandomizableContainer, ContainerSingleItem.BlockContainerSingleItem {

    public PotVariant variant;
    private final EnumSet<PotModifier> modifiers = EnumSet.noneOf(PotModifier.class);
    private PotionContents storedPotion = PotionContents.EMPTY;
    private @Nullable ResourceLocation potionTableId;
    private long potionTableSeed = 0L;
    public boolean skipBreakEffects;
    public boolean shouldDropItems;
    public long wobbleStartedAtTick;
    public @Nullable DecoratedPotBlockEntity.WobbleStyle lastWobbleStyle;
    private ItemStack item = ItemStack.EMPTY;
    protected @Nullable ResourceKey<LootTable> lootTable;
    protected long lootTableSeed = 0L;

    public PotBlockEntity(BlockPos pos, BlockState state) {
        super(NMLBlockEntities.POT.get(), pos, state);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (variant != null && level != null) {
            Optional.ofNullable(level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
                tag.putString("Variant", key.toString());
            });
        }

        if (!modifiers.isEmpty()) {
            ListTag modList = new ListTag();
            for (PotModifier mod : modifiers) {
                modList.add(StringTag.valueOf(mod.getSerializedName()));
            }
            tag.put("Modifiers", modList);
        }

        if (potionTableId != null) {
            tag.putString("PotionTable", potionTableId.toString());
            if (potionTableSeed != 0L) tag.putLong("PotionTableSeed", potionTableSeed);
        } else if (!storedPotion.equals(PotionContents.EMPTY)) {
            Tag potionTag = PotionContents.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), storedPotion).getOrThrow();
            tag.put("StoredPotion", potionTag);
        }

        if (!this.trySaveLootTable(tag) && !item.isEmpty()) {
            tag.put("Item", item.save(registries));
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("Variant")) {
            variant = registries.lookupOrThrow(NMLRegistries.POT_VARIANT_KEY).get(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, ResourceLocation.parse(tag.getString("Variant")))).orElseThrow().value();
        }

        modifiers.clear();
        if (tag.contains("Modifiers")) {
            ListTag modList = tag.getList("Modifiers", Tag.TAG_STRING);
            for (int i = 0; i < modList.size(); i++) {
                try {
                    modifiers.add(PotModifier.valueOf(modList.getString(i).toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        potionTableId = null;
        storedPotion = PotionContents.EMPTY;
        if (tag.contains("PotionTable")) {
            potionTableId = ResourceLocation.parse(tag.getString("PotionTable"));
            potionTableSeed = tag.contains("PotionTableSeed", Tag.TAG_LONG) ? tag.getLong("PotionTableSeed") : 0L;
        } else if (tag.contains("StoredPotion")) {
            PotionContents.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("StoredPotion"))
                    .resultOrPartial().ifPresent(p -> storedPotion = p);
        }

        if (!this.tryLoadLootTable(tag)) {
            item = ItemStack.parseOptional(registries, tag.getCompound("Item"));
        }
    }

    public void restoreFromFallingBlock(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }

    public ItemStack getPotAsItem() {
        ItemStack itemstack = getBlockState().getBlock().asItem().getDefaultInstance();
        itemstack.applyComponents(this.collectComponents());
        return itemstack;
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (level != null && variant != null) {
            Optional.ofNullable(level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getKey(variant)).ifPresent(key -> {
                components.set(NMLDataComponents.POT_VARIANT, key);
            });
        }

        if (!modifiers.isEmpty()) {
            components.set(NMLDataComponents.POT_MODIFIERS, modifiers.stream().map(PotModifier::getSerializedName).toList());
        }

        if (potionTableId != null) {
            components.set(NMLDataComponents.POT_POTION_TABLE, new SeededPotionTable(potionTableId, potionTableSeed));
        } else if (!storedPotion.equals(PotionContents.EMPTY)) {
            components.set(DataComponents.POTION_CONTENTS, storedPotion);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        Optional.ofNullable(componentInput.get(NMLDataComponents.POT_VARIANT)).ifPresent(key -> {
            variant = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, key)).orElse(null);
        });

        modifiers.clear();
        java.util.List<String> modList = componentInput.getOrDefault(NMLDataComponents.POT_MODIFIERS, List.of());
        for (String name : modList) {
            try {
                modifiers.add(PotModifier.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        SeededPotionTable potionTableComponent = componentInput.get(NMLDataComponents.POT_POTION_TABLE);
        if (potionTableComponent != null) {
            this.potionTableId = potionTableComponent.potionTable();
            this.potionTableSeed = potionTableComponent.seed();
        } else {
            PotionContents potion = componentInput.get(DataComponents.POTION_CONTENTS);
            if (potion != null) {
                storedPotion = potion;
            }
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Variant");
        tag.remove("Item");
        tag.remove("LootTable");
        tag.remove("LootTableSeed");
        tag.remove("PotionTable");
        tag.remove("PotionTableSeed");
        tag.remove("Modifiers");
        tag.remove("StoredPotion");
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


    public ItemStack getTheItem() {
        this.unpackLootTable(null);
        return this.item;
    }

    public ItemStack splitTheItem(int amount) {
        this.unpackLootTable(null);
        ItemStack itemstack = this.item.split(amount);
        if (this.item.isEmpty()) {
            this.item = ItemStack.EMPTY;
        }

        return itemstack;
    }

    public void setTheItem(ItemStack item) {
        this.unpackLootTable(null);
        this.item = item;
    }

    public void setFromItem(ItemStack item) {
        this.applyComponentsFromItemStack(item);
    }

    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    public boolean insert(ItemStack stack) {
        this.unpackLootTable(null);

        if (item.isEmpty() || (item.getMaxStackSize() > item.getCount() + stack.getCount() && ItemStack.isSameItemSameComponents(item, stack))) {
            setTheItem(stack.copyWithCount(item.getCount() + stack.getCount()));
            return true;
        }

        return false;
    }

    public boolean isLiving() {
        return hasModifier(PotModifier.ALIVE);
    }

    public Set<PotModifier> getModifiers() {
        return modifiers;
    }

    public boolean hasModifier(PotModifier modifier) {
        return modifiers.contains(modifier);
    }

    public void addModifier(PotModifier modifier) {
        modifiers.add(modifier);
        setChanged();
    }

    public void removeModifier(PotModifier modifier) {
        modifiers.remove(modifier);
        setChanged();
    }

    public PotionContents getStoredPotion() {
        unpackPotionTable();
        return storedPotion;
    }

    public void setStoredPotion(PotionContents potion) {
        this.storedPotion = potion;
        this.potionTableId = null;
        setChanged();
    }

    public void setPotionTable(ResourceLocation potionTableId, long seed) {
        this.potionTableId = potionTableId;
        this.potionTableSeed = seed;
    }

    @Nullable
    public ResourceLocation getPotionTableId() {
        return potionTableId;
    }

    public long getPotionTableSeed() {
        return potionTableSeed;
    }

    private void unpackPotionTable() {
        if (potionTableId != null && level != null && !level.isClientSide) {
            Registry<PotionTable> registry = level.registryAccess().registryOrThrow(NMLRegistries.POTION_TABLE_KEY);
            PotionTable table = registry.getOptional(ResourceKey.create(NMLRegistries.POTION_TABLE_KEY, potionTableId)).orElse(null);
            if (table != null) {
                RandomSource random = potionTableSeed != 0L ? RandomSource.create(potionTableSeed) : level.getRandom();
                storedPotion = table.select(random);
            }
            potionTableId = null;
            setChanged();
        }
    }

    public void wakeUp(@Nullable LivingEntity disturber) {
        if (level == null || level.isClientSide) return;
        BlockPos pos = getBlockPos();

        LivingPot pot = LivingPot.fromPot(this);
        if (disturber instanceof Player player && player.canBeSeenAsEnemy()) {
            pot.startPersistentAngerTimer();
            pot.setPersistentAngerTarget(player.getUUID());
            if (!player.isInvisible()) {
                pot.setTarget(player);
            }
        }
        level.addFreshEntity(pot);
        pot.startWakeUp();
        this.skipBreakEffects = true;
        level.removeBlock(pos, false);

        level.playSound(null, pos, SoundEvents.DECORATED_POT_STEP, SoundSource.HOSTILE, 1.0F, 0.8F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.DUST_PLUME,
                    pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                    8, 0.3, 0.05, 0.3, 0.02);
        }
    }

    public void wakeUpSilent() {
        if (level == null || level.isClientSide) return;
        BlockPos pos = getBlockPos();

        LivingPot pot = LivingPot.fromPot(this);
        level.addFreshEntity(pot);
        this.skipBreakEffects = true;
        level.removeBlock(pos, false);
    }

    public float getFullness() {
        this.unpackLootTable(null);

        return (float) item.getCount() / item.getMaxStackSize();
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
