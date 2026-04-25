package com.farcr.nomansland.common.friend.offering;

import com.farcr.nomansland.common.friend.FriendMoon;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;

public class OfferingContext {
    final Entity entity;
    public Entity getEntity() {
        return this.entity;
    }
    final ResourceLocation dialogueLocation;
    public ResourceLocation getDialogueLocation() {
        return this.dialogueLocation;
    }

    final OfferingType offeringType;
    public OfferingType getOfferingType() {
        return this.offeringType;
    }

    public OfferingContext(final Entity entity, ResourceLocation dialogueLocation) {
        this.entity = entity;
        this.dialogueLocation = dialogueLocation;
        // calculate offering type once
        this.offeringType = FriendMoon.getOfferingType(this);
    }

    public boolean isValid() {
        if (entity instanceof ItemEntity itemEntity) {
            if (itemEntity.getItem().getItem() == Items.AIR)
                return false;
        }
        return entity != null && entity.isAlive();
    }
}