package com.farcr.nomansland.common.world.densityfunction.modification;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class DensityFunctionRegistryCodec extends RegistryFileCodec<DensityFunction> {
    public DensityFunctionRegistryCodec(Codec<DensityFunction> elementCodec) {
        super(Registries.DENSITY_FUNCTION, elementCodec, true);
    }

    @Override
    public <T> DataResult<Pair<Holder<DensityFunction>, T>> decode(DynamicOps<T> ops, T input) {
        if (ops instanceof RegistryOps<?> registryops) {
            Optional<HolderGetter<DensityFunction>> optional = registryops.getter(this.registryKey);
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Registry does not exist: " + this.registryKey);
            } else {
                HolderGetter<DensityFunction> registry = optional.get();
                DataResult<Pair<ResourceLocation, T>> dataresult = ResourceLocation.CODEC.decode(ops, input);
                if (dataresult.result().isEmpty()) {
                    return this.elementCodec.decode(ops, input).map(dataResult -> dataResult.mapFirst(Holder::direct));
                } else {
                    Pair<ResourceLocation, T> pair = dataresult.result().get();
                    ResourceKey<DensityFunction> resourcekey = ResourceKey.create(this.registryKey, pair.getFirst());
                    boolean hasModifier = DensityFunctionModifications.MODIFIERS.containsKey(resourcekey);

                    return registry.get(resourcekey)
                            .map(DataResult::success)
                            .orElseGet(() ->
                                    DataResult.error(() -> "Failed to get element " + resourcekey)
                            )
                            .map(holder ->
                                    Pair.of(
                                            // if the function is modified, swap it out for the modified reference holder.
                                            hasModifier ? new ModifiedHolderWrapper(holder, DensityFunctionModifications.MODIFIERS.get(resourcekey)) : holder,
                                            pair.getSecond()
                                    )
                            )
                            .setLifecycle(Lifecycle.stable());
                }
            }
        } else {
            return this.elementCodec.decode(ops, input).map(holder -> holder.mapFirst(Holder::direct));
        }
    }

    @Override
    public String toString() {
        return "ModifiedRegistryFileCodec[" + this.registryKey + " " + this.elementCodec + "]";
    }

    private static final class ModifiedHolderWrapper implements Holder<DensityFunction> {
        private final Holder<DensityFunction> wrapped;
        private final DensityFunctionModifier modifier;
        private DensityFunction modifiedValue;

        private ModifiedHolderWrapper(Holder<DensityFunction> wrapped, DensityFunctionModifier modifier) {
            this.wrapped = wrapped;
            this.modifier = modifier;
        }

        @Override
        public DensityFunction value() {
            if (modifiedValue == null) {
                modifiedValue = modifier.visit(wrapped.value());
            }
            return modifiedValue;
        }

        @Override
        public boolean isBound() {
            return wrapped.isBound();
        }
        @Override
        public boolean is(ResourceLocation location) {
            return wrapped.is(location);
        }
        @Override
        public boolean is(ResourceKey<DensityFunction> resourceKey) {
            return wrapped.is(resourceKey);
        }
        @Override
        public boolean is(Predicate<ResourceKey<DensityFunction>> predicate) {
            return wrapped.is(predicate);
        }
        @Override
        public boolean is(TagKey<DensityFunction> tagKey) {
            return wrapped.is(tagKey);
        }
        @Override
        public boolean is(Holder<DensityFunction> holder) {
            return wrapped.is(holder);
        }
        @Override
        public Stream<TagKey<DensityFunction>> tags() {
            return wrapped.tags();
        }
        @Override
        public Either<ResourceKey<DensityFunction>, DensityFunction> unwrap() {
            return wrapped.unwrap();
        }
        @Override
        public Optional<ResourceKey<DensityFunction>> unwrapKey() {
            return wrapped.unwrapKey();
        }
        @Override
        public Kind kind() {
            return wrapped.kind();
        }
        @Override
        public boolean canSerializeIn(HolderOwner<DensityFunction> owner) {
            return wrapped.canSerializeIn(owner);
        }
    }

    private static class ModifiedReferenceHolder extends Holder.Reference<DensityFunction> {
        private final DensityFunctionModifier modifier;
        private boolean appliedModifier = false;

        protected ModifiedReferenceHolder(Holder.Reference<DensityFunction> holder, DensityFunctionModifier modifier) {
            //super(holder.type, holder.owner, holder.key, holder.value == null ? null : Util.make(() -> modifier.apply(holder.value)));
            super(holder.type, holder.owner, holder.key, holder.value);
            this.modifier = modifier;
        }

        @Override
        public DensityFunction value() {
            if (this.value == null) {
                throw new IllegalStateException("Trying to access unbound value '" + this.key + "' from registry " + this.owner);
            }

            if (!this.appliedModifier) {
                this.value = modifier.visit(value);
                this.appliedModifier = true;
            }
            return super.value();
        }

        //        @Override
//        protected void bindValue(DensityFunction densityFunction) {
//            super.bindValue(modifier.visit(densityFunction));
//        }
    }
}
