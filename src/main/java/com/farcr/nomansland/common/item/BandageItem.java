package com.farcr.nomansland.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BandageItem extends Item {
    protected static final ThreadLocal<LivingEntity> TARGET_ENTITY = new ThreadLocal<>();

    public BandageItem(Properties properties) {
        super(properties);
    }

    protected LivingEntity getTargetEntity(LivingEntity user) {
        LivingEntity target = TARGET_ENTITY.get();
        if (target == null || target.isDeadOrDying()) {
            return user;
        }
        return target;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, LivingEntity target, @NotNull InteractionHand hand) {
        if (!target.isDeadOrDying() && target != player) {
            TARGET_ENTITY.set(target);
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        Player player = user instanceof Player ? (Player) user : null;
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        }

        LivingEntity target = getTargetEntity(user);
        TARGET_ENTITY.remove();

        PotionContents potionContents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        potionContents.forEachEffect((effect) -> {
            if (effect.getEffect().value().isInstantenous()) {
                effect.getEffect().value().applyInstantenousEffect(player, player, target, effect.getAmplifier(), 1);
            } else {
                target.addEffect(effect);
            }
        });

        target.heal(4);

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            stack.consume(1, player);
            player.getCooldowns().addCooldown(this, 140);
        }

        return stack;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity user, @NotNull ItemStack stack, int remainingUseDuration) {
        LivingEntity target = TARGET_ENTITY.get();
        if (target != null && user instanceof Player player) {
            if (target.isDeadOrDying() || player.distanceTo(target) > 4.0) {
                TARGET_ENTITY.remove();
                player.stopUsingItem();
            }
        }
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user, int timeCharged) {
        TARGET_ENTITY.remove();
    }

    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 40;
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        LivingEntity target = TARGET_ENTITY.get();
        if (target != null) {
            return UseAnim.CROSSBOW;
        }
        return UseAnim.EAT;
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    public void appendHoverText(ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) {
            potionContents.addPotionTooltip(tooltipComponents::add, 1, context.tickRate());
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) {
            for (var effect : potionContents.getAllEffects()) {
                String effectKey = effect.getEffect().value().getDescriptionId();
                String effectName = effectKey.substring(effectKey.lastIndexOf('.') + 1);
                return Component.translatable("item.nomansland.bandage.effect." + effectName);
            }
        }
        return super.getName(stack);
    }
}
