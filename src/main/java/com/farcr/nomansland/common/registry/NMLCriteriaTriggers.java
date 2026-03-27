package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.trigger.BuddyAscensionTrigger;
import com.farcr.nomansland.common.trigger.DreamFriendMoonTrigger;
import com.farcr.nomansland.common.trigger.MeetFriendMoonTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.DefaultBlockInteractionTrigger;
import net.minecraft.advancements.critereon.DistanceTrigger;
import net.minecraft.advancements.critereon.KilledTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLCriteriaTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, NoMansLand.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, KilledTrigger> KILL_MOB_NEAR_MONSTER_ANCHOR = TRIGGERS.register("kill_mob_near_monster_anchor", KilledTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, DistanceTrigger> CLIMB_UP_HEIGHT = TRIGGERS.register("climb_up_height", DistanceTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, KilledTrigger> IGNITE_FLAMMABLE_ENTITY = TRIGGERS.register("ignite_flammable_entity", KilledTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, DefaultBlockInteractionTrigger> MINE_ORE_WITH_EXPLOSIVE = TRIGGERS.register("mine_ore_with_explosive", DefaultBlockInteractionTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, MeetFriendMoonTrigger> MEET_FRIEND_MOON = TRIGGERS.register("meet_friend_moon_trigger", MeetFriendMoonTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, DreamFriendMoonTrigger> DREAM_FRIEND_MOON = TRIGGERS.register("dream_friend_moon_trigger", DreamFriendMoonTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, BuddyAscensionTrigger> BUDDY_ASCENSION = TRIGGERS.register("buddy_ascension", BuddyAscensionTrigger::new);
}
