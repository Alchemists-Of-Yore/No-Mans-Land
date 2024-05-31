package com.farcr.nomansland.core.content.lootmodifiers;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public class AddLootTableModifier extends LootModifier {
    public static final Supplier<Codec<AddLootTableModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create((inst) -> codecStart(inst).and(ResourceLocation.CODEC.fieldOf("lootTable").forGetter((m) -> m.lootTable)).apply(inst, AddLootTableModifier::new)));
    private final ResourceLocation lootTable;

    protected AddLootTableModifier(LootItemCondition[] conditionsIn, ResourceLocation lootTable) {
        super(conditionsIn);
        this.lootTable = lootTable;
    }

    @Override
    public @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            LootTable extraTable = context.getResolver().getLootTable(this.lootTable);
            ServerLevel level = context.getLevel();
            Objects.requireNonNull(generatedLoot);
            extraTable.getRandomItemsRaw(context, LootTable.createStackSplitter(level, generatedLoot::add));

        return generatedLoot;
    }
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
