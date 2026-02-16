package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.client.model.armor.*;
import net.minecraft.client.*;
import net.minecraft.client.model.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.*;
import net.neoforged.neoforge.client.extensions.common.*;
import org.jetbrains.annotations.*;

import java.util.function.*;

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