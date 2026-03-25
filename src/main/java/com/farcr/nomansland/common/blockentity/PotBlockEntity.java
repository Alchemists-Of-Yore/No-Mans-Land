package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.pots.PotModifier;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemContainerContents;
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
    public boolean skipBreakEffects;
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

        if (!storedPotion.equals(PotionContents.EMPTY)) {
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

        storedPotion = PotionContents.EMPTY;
        if (tag.contains("StoredPotion")) {
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

        if (!this.item.isEmpty()) {
            components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(this.item)));
        }

        if (!modifiers.isEmpty()) {
            components.set(NMLDataComponents.POT_MODIFIERS, modifiers.stream().map(PotModifier::getSerializedName).toList());
        }

        if (!storedPotion.equals(PotionContents.EMPTY)) {
            components.set(DataComponents.POTION_CONTENTS, storedPotion);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);

        Optional.ofNullable(componentInput.get(NMLDataComponents.POT_VARIANT)).ifPresent(key -> {
            variant = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, key)).orElse(null);
        });

        this.item = componentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();

        modifiers.clear();
        java.util.List<String> modList = componentInput.getOrDefault(NMLDataComponents.POT_MODIFIERS, List.of());
        for (String name : modList) {
            try {
                modifiers.add(PotModifier.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        PotionContents potion = componentInput.get(DataComponents.POTION_CONTENTS);
        if (potion != null) {
            storedPotion = potion;
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("Variant");
        tag.remove("Item");
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
        return storedPotion;
    }

    public void setStoredPotion(PotionContents potion) {
        this.storedPotion = potion;
        setChanged();
    }

    public void wakeUp(@Nullable LivingEntity disturber) {
        if (level == null || level.isClientSide) return;
        BlockPos pos = getBlockPos();

        LivingPot pot = new LivingPot(NMLEntities.LIVING_POT.get(), level);
        pot.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        float yaw = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
        pot.setYRot(yaw);
        pot.yRotO = yaw;
        pot.setYBodyRot(yaw);
        pot.setYHeadRot(yaw);
        pot.setVariant(variant, getBlockState());
        pot.setModifiers(modifiers);
        if (!storedPotion.equals(PotionContents.EMPTY)) {
            pot.setStoredPotion(storedPotion);
        }
        pot.setHomePos(pos);
        if (this.lootTable != null) {
            pot.setLootTable(this.lootTable);
            pot.setLootTableSeed(this.lootTableSeed);
        } else if (!item.isEmpty()) {
            pot.setStoredItem(item);
        }
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

        LivingPot pot = new LivingPot(NMLEntities.LIVING_POT.get(), level);
        pot.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        float yaw = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
        pot.setYRot(yaw);
        pot.yRotO = yaw;
        pot.setYBodyRot(yaw);
        pot.setYHeadRot(yaw);
        pot.setVariant(variant, getBlockState());
        pot.setModifiers(modifiers);
        if (!storedPotion.equals(PotionContents.EMPTY)) {
            pot.setStoredPotion(storedPotion);
        }
        pot.setHomePos(pos);
        if (this.lootTable != null) {
            pot.setLootTable(this.lootTable);
            pot.setLootTableSeed(this.lootTableSeed);
        } else if (!item.isEmpty()) {
            pot.setStoredItem(item);
        }
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
