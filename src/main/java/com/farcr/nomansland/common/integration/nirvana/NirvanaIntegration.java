package com.farcr.nomansland.common.integration.nirvana;

import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

import static com.farcr.nomansland.common.registry.entities.NMLEntities.ENTITIES;

public class NirvanaIntegration {
    public static final Supplier<EntityType<FatJoint>> FAT_JOINT =
            ENTITIES.register("fat_joint", () -> EntityType.Builder.<FatJoint>of(FatJoint::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(4).updateInterval(20).build("fat_joint"));

    public static final ItemDefinition<Item> FAT_JOINT_ITEM = NMLItems.register("fat_joint",
            () -> new FatJointItem(new Item.Properties().stacksTo(8)));

    public static void register() {
    }
}
