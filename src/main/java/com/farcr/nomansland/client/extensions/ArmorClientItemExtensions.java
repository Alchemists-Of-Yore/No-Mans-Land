package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.client.model.armor.*;
import net.minecraft.client.*;
import net.minecraft.client.model.*;
import net.minecraft.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.extensions.common.*;
import org.jetbrains.annotations.*;

import java.util.function.*;

/**
 * @author WireSegal
 * Created at 10:05 PM on 1/1/25.
 */
public class ArmorClientItemExtensions implements IClientItemExtensions {
	private final Supplier<LodestoneArmorModel> model;

	public ArmorClientItemExtensions(Supplier<LodestoneArmorModel> model) {
		this.model = model;
	}

	@Override
	public @NotNull LodestoneArmorModel getHumanoidArmorModel(LivingEntity entity, @NotNull ItemStack itemStack, @NotNull EquipmentSlot armorSlot, @NotNull HumanoidModel playerModel) {
		float pticks = (float) (Minecraft.getInstance().getFrameTimeNs() / 20000000000L);
		float f = Mth.rotLerp(pticks, entity.yBodyRotO, entity.yBodyRot);
		float f1 = Mth.rotLerp(pticks, entity.yHeadRotO, entity.yHeadRot);
		float netHeadYaw = f1 - f;
		float netHeadPitch = Mth.lerp(pticks, entity.xRotO, entity.getXRot());
		LodestoneArmorModel model = this.model.get();
		model.slot = armorSlot;
		model.copyFromDefault(playerModel);
		model.setupAnim(entity, entity.walkAnimation.position(), entity.walkAnimation.speed(), entity.tickCount + pticks, netHeadYaw, netHeadPitch);
		return model;
	}
}