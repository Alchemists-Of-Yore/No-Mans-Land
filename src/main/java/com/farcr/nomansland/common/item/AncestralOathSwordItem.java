package com.farcr.nomansland.common.item;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.common.networking.alchemist_tools.ClientboundOathSwordParried;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class AncestralOathSwordItem extends SwordItem {
    public AncestralOathSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BLOCK;
    }

    public static final float SWORD_PARRY_TICKS = 10f;
    private static final int STASIS_RANGE = 3;

    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 72000;
    }

    public static boolean canHurtUnderOath(Entity entity) {
        return (entity instanceof LivingEntity livingEntity && livingEntity.hasEffect(NMLEffects.STASIS))
            || entity.getType().is(NMLTags.MALEVOLENT_ENTITIES);
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        return canHurtUnderOath(target);
    }

    public float getParryTiming(ItemStack itemStack, LivingEntity livingEntity) {
        return itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks();
    }
    public boolean withinParryTiming(ItemStack itemStack, LivingEntity livingEntity) {
        return (getParryTiming(itemStack, livingEntity) <= SWORD_PARRY_TICKS);
    }

    public void handleBlockingEvent(LivingIncomingDamageEvent event, ItemStack itemStack) {
        LivingEntity damagedEntity = event.getEntity();
        Entity directEntity = event.getSource().getDirectEntity();
        if (directEntity instanceof Projectile projectile) {

        }

        if (withinParryTiming(itemStack, damagedEntity)) {
//            NoMansLand.LOGGER.info("perfect parry timing");
            damagedEntity.level().playSound(
                null, damagedEntity.blockPosition(),
                NMLSounds.OATH_PARRY.get(), SoundSource.PLAYERS
            );
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                damagedEntity, new ClientboundOathSwordParried(damagedEntity.getId())
            );
        }

        // if damage is from an entity
        if (directEntity instanceof LivingEntity livingEntity) {
            // "parry miss" effect
            damagedEntity.level().playSound(
                null, damagedEntity.blockPosition(),
                NMLSounds.OATH_BLOCK.get(), SoundSource.PLAYERS
            );
            event.setAmount(event.getAmount() * .25f);
            livingEntity.knockback(0.25, damagedEntity.getX() - livingEntity.getX(), damagedEntity.getZ() - livingEntity.getZ());
            livingEntity.addEffect(new MobEffectInstance(
                NMLEffects.STASIS, (40 + (int) (20 * event.getOriginalAmount()))
            ));
        }
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
