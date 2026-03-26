package com.farcr.nomansland.common.entity.variant_action;

import com.google.gson.JsonObject;
import dev.tazer.mixed_litter.actions.VariantActionType;
import net.minecraft.resources.ResourceLocation;

public class SetAntlerLayer implements VariantActionType {
    public ResourceLocation texture;
    
    @Override
    public VariantActionType resolve(JsonObject actionsArgs, JsonObject variantArgs, JsonObject defaultArgs) {
        JsonObject arguments = VariantActionType.resolveArguments(actionsArgs, variantArgs, defaultArgs);
        texture = ResourceLocation.parse(arguments.get("texture").getAsString()).withPath(path -> "textures/entity/" + path);
        return this;
    }
}
