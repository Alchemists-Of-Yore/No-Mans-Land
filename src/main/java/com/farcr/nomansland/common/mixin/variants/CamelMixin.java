package com.farcr.nomansland.common.mixin.variants;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.variant.CamelVariant;
import com.farcr.nomansland.common.mixin.MobMixin;
import com.farcr.nomansland.common.registry.NMLDataSerializers;
import com.farcr.nomansland.common.registry.NMLVariants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(Camel.class)
public abstract class CamelMixin extends MobMixin implements VariantHolder<Holder<CamelVariant>> {
    @Unique
    private static final EntityDataAccessor<Holder<CamelVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Camel.class, NMLDataSerializers.CAMEL_VARIANT.get());
    @Unique
    private static final String VARIANT_KEY = "variant";
    @Unique
    private static final ResourceKey<CamelVariant> DEFAULT_VARIANT = ResourceKey.create(NMLVariants.CAMEL_VARIANT_KEY, ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "default"));

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(DATA_VARIANT_ID, this.registryAccess().registryOrThrow(NMLVariants.CAMEL_VARIANT_KEY).getHolderOrThrow(DEFAULT_VARIANT));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        this.getVariant().unwrapKey().ifPresent((variant) -> {
            compound.putString(VARIANT_KEY, variant.location().toString());
        });
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound, CallbackInfo ci) {
        Optional.ofNullable(ResourceLocation.tryParse(compound.getString(VARIANT_KEY)))
                .map((string) -> ResourceKey.create(NMLVariants.CAMEL_VARIANT_KEY, string))
                .flatMap((variant) -> this.registryAccess().registryOrThrow(NMLVariants.CAMEL_VARIANT_KEY).getHolder(variant))
                .ifPresent(this::setVariant);
    }

    @Override
    protected void finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData spawnGroupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        Registry<CamelVariant> registry = this.registryAccess().registryOrThrow(NMLVariants.CAMEL_VARIANT_KEY);
        List<Holder.Reference<CamelVariant>> possibleVariants = registry.holders()
                .filter((v) -> v.value().biomes().isPresent() && v.value().biomes().get().contains(level.getBiome(this.blockPosition())))
                .toList();
        List<Holder.Reference<CamelVariant>> defaultVariants = registry.holders()
                .filter((v) -> v.value().biomes().isEmpty() || v.is(DEFAULT_VARIANT))
                .toList();
        this.setVariant(possibleVariants.isEmpty() ? defaultVariants.get(random.nextInt(defaultVariants.size())) : possibleVariants.get(random.nextInt(possibleVariants.size())));
    }

    @Override
    public Holder<CamelVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    public void setVariant(Holder<CamelVariant> variantHolder) {
        this.entityData.set(DATA_VARIANT_ID, variantHolder);
    }

    @Inject(method = "getBreedOffspring*", at = @At("RETURN"), cancellable = true)
    private void getBreedOffspring(ServerLevel level, AgeableMob otherParent, CallbackInfoReturnable<AgeableMob> cir) {
        Camel pig = EntityType.CAMEL.create(level);
        if (pig != null) {
            ((VariantHolder<Holder<CamelVariant>>)pig).setVariant(this.random.nextBoolean() ?  this.getVariant() : ((VariantHolder<Holder<CamelVariant>>)otherParent).getVariant());
        }
        cir.setReturnValue(pig);
    }
}
