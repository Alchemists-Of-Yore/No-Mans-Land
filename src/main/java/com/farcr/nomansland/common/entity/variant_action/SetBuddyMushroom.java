package com.farcr.nomansland.common.entity.variant_action;

import com.google.gson.JsonObject;
import dev.tazer.mixed_litter.actions.VariantActionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class SetBuddyMushroom implements VariantActionType {
    private boolean resolved;
    private Block block;

    public Block getBlock() {
        return resolved ? block : null;
    }

    @Override
    public VariantActionType resolve(JsonObject actionsArgs, JsonObject variantArgs, JsonObject defaultArgs) {
        SetBuddyMushroom result = new SetBuddyMushroom();
        JsonObject arguments = VariantActionType.resolveArguments(actionsArgs, variantArgs, defaultArgs);
        result.block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(arguments.get("mushroom").getAsString()));
        result.resolved = true;
        return result;
    }
}
