package com.farcr.nomansland.common.registry.entities;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.LingeringCloud;
import com.farcr.nomansland.common.entity.Moose;
import com.farcr.nomansland.common.entity.billhook_bass.BillhookBass;
import com.farcr.nomansland.common.entity.bombs.ExplosiveEntity;
import com.farcr.nomansland.common.entity.bombs.FirebombEntity;
import com.farcr.nomansland.common.entity.bombs.InkBombEntity;
import com.farcr.nomansland.common.entity.bombs.LivingUrnEntity;
import com.farcr.nomansland.common.entity.deer.Deer;
import com.farcr.nomansland.common.entity.goose.Goose;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NoMansLand.MODID);

//    public static final Supplier<EntityType<Boat>> BOAT =
//            ENTITIES.register("maple_boat", () -> EntityType.Builder.<Boat>of(Boat::new, MobCategory.MISC)
//                    .sized(1.375f, 0.5625f).build("maple_boat"));
//    public static final Supplier<EntityType<ChestBoat>> CHEST_BOAT =
//            ENTITIES.register("maple_chest_boat", () -> EntityType.Builder.<ChestBoat>of(ChestBoat::new, MobCategory.MISC)
//                    .sized(1.375f, 0.5625f).build("maple_chest_boat"));

    public static final Supplier<EntityType<FirebombEntity>> FIREBOMB =
            ENTITIES.register("firebomb", () -> EntityType.Builder.<FirebombEntity>of(FirebombEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(20).build("firebomb"));

    public static final Supplier<EntityType<InkBombEntity>> INK_BOMB =
            ENTITIES.register("ink_bomb", () -> EntityType.Builder.<InkBombEntity>of(InkBombEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(20).build("ink_bomb"));

    public static final Supplier<EntityType<ExplosiveEntity>> EXPLOSIVE =
            ENTITIES.register("explosive", () -> EntityType.Builder.<ExplosiveEntity>of(ExplosiveEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(4).updateInterval(20).build("explosive"));

    public static final Supplier<EntityType<LivingUrnEntity>> LIVING_URN =
            ENTITIES.register("living_urn", () -> EntityType.Builder.<LivingUrnEntity>of(LivingUrnEntity::new, MobCategory.MISC)
                    .sized(0.375F, 0.375F).clientTrackingRange(4).updateInterval(20).build("living_urn"));

    public static final Supplier<EntityType<LingeringCloud>> LINGERING_CLOUD =
            ENTITIES.register("lingering_cloud", () -> EntityType.Builder.<LingeringCloud>of(LingeringCloud::new, MobCategory.MISC)
                    .fireImmune().sized(6, 6).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE).build("lingering_cloud"));

    public static final Supplier<EntityType<BillhookBass>> BILLHOOK_BASS =
            ENTITIES.register("billhook_bass", () -> EntityType.Builder.of(BillhookBass::new, MobCategory.WATER_CREATURE)
            .sized(0.7F, 0.5F).build("billhook_bass"));

    public static final Supplier<EntityType<Deer>> DEER =
            ENTITIES.register("deer", () -> EntityType.Builder.of(Deer::new, MobCategory.CREATURE)
                    .sized(0.8F, 1.4F).build("deer"));

    public static final Supplier<EntityType<Goose>> GOOSE =
            ENTITIES.register("goose", () -> EntityType.Builder.of(Goose::new, MobCategory.CREATURE)
                    .sized(0.8F, 1).build("goose"));

//    public static final Supplier<EntityType<BuriedEntity>> BURIED =
//            ENTITIES.register("buried", () -> EntityType.Builder.of(BuriedEntity::new, MobCategory.MONSTER)
//                    .sized(1.0f, 1.0f).clientTrackingRange(8).build("buried"));
//
    public static final Supplier<EntityType<Moose>> MOOSE =
            ENTITIES.register("moose", () -> EntityType.Builder.of(Moose::new, MobCategory.CREATURE)
                    .sized(1.5f, 2.5f).build("moose"));

}
