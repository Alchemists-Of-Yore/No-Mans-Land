package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.client.model.armor.LodestoneArmorModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @author SammySemicolon
 */
public class LodestoneArmorClientItemExtensions implements IClientItemExtensions {
	private final Supplier<? extends Model> model;

	public LodestoneArmorClientItemExtensions(Supplier<? extends Model> model) {
		this.model = model;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public @NotNull Model getGenericArmorModel(@NotNull LivingEntity entity, @NotNull ItemStack itemStack, @NotNull EquipmentSlot armorSlot, @NotNull HumanoidModel playerModel) {
		var model = this.model.get();
		if (model instanceof EntityModel entityModel) {
			float partialTicks = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true);
			float f = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
			float f1 = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);

			float walkPosition = entity.walkAnimation.position();
			float walkSpeed = entity.walkAnimation.speed();
			float tickCount = entity.tickCount + partialTicks;

			float netHeadYaw = f1 - f;
			float netHeadPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

			if (entityModel instanceof LodestoneArmorModel armorModel) {
				armorModel.slot = armorSlot;
				armorModel.copyFromDefault(playerModel);
			}
			entityModel.setupAnim(entity, walkPosition, walkSpeed, tickCount, netHeadYaw, netHeadPitch);
			if (entityModel instanceof HumanoidModel<?> humanoidModel) {
				ClientHooks.copyModelProperties(playerModel, humanoidModel);
			}
			else {
				playerModel.copyPropertiesTo(entityModel);
			}
		}
		return model;
	}
}