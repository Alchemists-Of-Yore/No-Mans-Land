package com.farcr.nomansland.common.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.NotNull;

public class AncestralOathSwordItem extends SwordItem {
    public AncestralOathSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    public static boolean canHurtUnderOath(Entity entity) {
        return false;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        return canHurtUnderOath(target);
    }

    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }

    public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SHIELD_ACTIONS.contains(itemAbility);
    }
}
