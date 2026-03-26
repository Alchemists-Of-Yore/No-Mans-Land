package com.farcr.nomansland.common.trigger;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class BuddyAscensionTrigger extends SimpleCriterionTrigger<BuddyAscensionTrigger.BuddyAscensionInstance> {
    @Override
    public Codec<BuddyAscensionInstance> codec() {
        return BuddyAscensionInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> true);
    }

    public record BuddyAscensionInstance() implements SimpleInstance {
        public static final Codec<BuddyAscensionInstance> CODEC =
            Codec.unit(new BuddyAscensionInstance());

        @Override
        public void validate(CriterionValidator validator) {
            SimpleInstance.super.validate(validator);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
