package com.farcr.nomansland.client.renderer.context;

import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;

public class StasisEntityContext {
    private static Entity ENTITY_CONTEXT;
    public static Entity getEntityContext() { return ENTITY_CONTEXT; }
    public static <T extends Entity> void setEntityContext(T entity) { ENTITY_CONTEXT = entity; }
    public static void clearEntityContext() { ENTITY_CONTEXT = null; }
}
