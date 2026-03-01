package com.farcr.nomansland.common.entity.buddy;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.List;
import java.util.Optional;

public class Buddy extends PathfinderMob implements Npc {

    private Registry<BuddyFood> buddyFoods;
    private void setBuddyFood(Registry<BuddyFood> registry) {
        this.buddyFoods = registry;
    }

    public Buddy(EntityType<? extends Buddy> entityType, Level level) {
        super(entityType, level);

        level.registryAccess().registry(NMLRegistries.BUDDY_FOOD_KEY)
            .ifPresent(this::setBuddyFood);
    }

    private static final int SUSPICIOUS_STEW_MULTIPLIER = 10;

    private BlockPos anchorPosition;
    public boolean isNaturallySpawned() {
        return (anchorPosition != null);
    }

    public static final List<String> COPY_ON_RESPAWN = List.of(
        "CustomName"
    );

    public void prepareAnchor(BlockPos anchorPosition) {
        this.anchorPosition = anchorPosition;
        this.restrictTo(anchorPosition, 5);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        if (anchorPosition != null)
            tag.put("BuddyAnchorPosition", NbtUtils.writeBlockPos(anchorPosition));
        super.addAdditionalSaveData(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        NbtUtils.readBlockPos(tag, "BuddyAnchorPosition").ifPresent(this::prepareAnchor);
        super.readAdditionalSaveData(tag);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 50f)
            .add(Attributes.KNOCKBACK_RESISTANCE, -.5f);
    }

    @Override
    public void tick() {
        if (this.isAlive())
            setHealth(Math.min(getHealth() + (1f / 20f), getMaxHealth()));
        super.tick();

        if (level().isClientSide)
            crouchTimer = Math.max(0, crouchTimer - 1);
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();
        getBrain().tick(level, this);
        BuddyAI.updateActivity(this);
        super.customServerAiStep();
    }

    private int crouchTimer = 0;
    public void crouch() {
        crouchTimer += 6;
    }

    @Override public boolean isCrouching() {
        return (crouchTimer > 0);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && buddyFoods != null) {
            ItemStack itemstack = player.getItemInHand(hand);
            Optional<BuddyFood> foodResult = buddyFoods.stream().filter(
                (foodType) -> foodType.item().contains(itemstack.getItemHolder())
            ).findFirst();
            if (foodResult.isPresent()) {
                if (!this.level().isClientSide) {
                    // So bowls are returned correctly
                    ItemStack resultingItem = itemstack.finishUsingItem(level(), player);
                    player.setItemInHand(hand, resultingItem);

                    this.playSound(
                        SoundEvents.PLAYER_BURP,
                        0.5F + 0.5F * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
                    );

                    // Apply suspicious stew effects tenfold
                    if (itemstack.is(Items.SUSPICIOUS_STEW)) {
                        SuspiciousStewEffects stew = itemstack.getOrDefault(
                            DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY);
                        for (SuspiciousStewEffects.Entry effect : stew.effects()) {
                            MobEffectInstance susEffect = effect.createEffectInstance();
                            this.addEffect(
                                new MobEffectInstance(
                                    susEffect.getEffect(),
                                    susEffect.getDuration() * SUSPICIOUS_STEW_MULTIPLIER,
                                    susEffect.getAmplifier(),
                                    false, true
                                )
                            );
                        }
                    }
                }
                // yaaay!!!
                this.addEffect(new MobEffectInstance(NMLEffects.HAPPINESS, foodResult.get().happinessTicks(), 0, true, false));
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    // ripped these from old minecraft please dont judge me too hard LOL
    public static void setupAnimationHappy(
        Entity entity, ModelPart head, ModelPart hat,
        ModelPart leftArm, ModelPart rightArm,
        ModelPart leftLeg, ModelPart rightLeg
    ) {
        double time = System.currentTimeMillis() / 100D;

        head.yRot = (float) Math.sin(time * 0.83D);
        head.xRot = (float) Math.sin(time) * 0.8F;
        hat.yRot = head.yRot;
        hat.xRot = head.xRot;

        rightArm.xRot = (float) Math.sin(time * 0.6662D + Math.PI) * 2.0F;
        rightArm.zRot = (float) (Math.sin(time * 0.2312D) + 1.0D);
        leftArm.xRot = (float) Math.sin(time * 0.6662D) * 2.0F;
        leftArm.zRot = (float) (Math.sin(time * 0.2812D) - 1.0D);
        rightLeg.xRot = (float) Math.sin(time * 0.6662D) * 1.4F;
        leftLeg.xRot = (float) Math.sin(time * 0.6662D + Math.PI) * 1.4F;
    }

    @Override
    protected Brain.Provider<Buddy> brainProvider() {
        return BuddyAI.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return BuddyAI.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<Buddy> getBrain() {
        return (Brain<Buddy>) super.getBrain();
    }

    public static boolean checkBuddySpawnRules(
        EntityType<? extends Buddy> buddy,
        LevelAccessor level, MobSpawnType spawnType,
        BlockPos pos, RandomSource random
    ) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override public void remove(Entity.RemovalReason reason) {
        if (isNaturallySpawned() && this.level() instanceof ServerLevel serverLevel) {
            if (!serverLevel.isClientSide && (reason.shouldDestroy()))
                BuddyChunkAnchor.getOrDefault(serverLevel).queryRespawn(anchorPosition, this);
        }
        super.remove(reason);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) { return false; }
}