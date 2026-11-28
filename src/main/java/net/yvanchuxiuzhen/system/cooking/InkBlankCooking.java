package net.yvanchuxiuzhen.system.cooking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.InkBlankRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.InkCategoryGroup;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.InkGradeTier;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.ItemRequirement;
import net.yvanchuxiuzhen.system.cooking.data.CookingTableRecipes;
import net.yvanchuxiuzhen.system.material.api.MaterialInstance;
import net.yvanchuxiuzhen.system.material.api.Materials;

/** 无菜谱墨料坯按配置规则统计材料槽均级分档，延迟交付重新查坐标并覆盖当时库存的首槽。 */
final class InkBlankCooking {
    private InkBlankCooking() {
    }

    static void begin(ServerPlayer player, CookingPotBlockEntity pot) {
        InkBlankRule rule = CookingTableRecipes.inkBlank();
        TagKey<Item> materialTag = ItemTags.create(ResourceLocation.parse(rule.materialTag()));
        Map<String, Integer> counts = new HashMap<>();
        Set<Integer> matchedCategories = new HashSet<>();
        double levelSum = 0;
        double materialSlots = 0;

        for (int slot = 0; slot < 9; slot++) {
            ItemStack input = pot.getItem(slot);
            String identity = BuiltInRegistries.ITEM.getKey(input.getItem()).toString();
            counts.merge(identity, input.getCount(), Integer::sum);
            if (input.is(materialTag)) {
                MaterialInstance material = Materials.instance(input);
                int category = category(input, rule);
                if (category != 0) matchedCategories.add(category);
                materialSlots++;
                levelSum += material.level();
            }
        }
        for (InkCategoryGroup group : rule.categoryGroups()) {
            if (!matchedCategories.contains(group.category())) return;
        }
        for (ItemRequirement reagent : rule.reagents()) {
            if (counts.getOrDefault(reagent.itemId(), 0) < reagent.count()) return;
        }

        double average = levelSum / materialSlots;
        String outputId = resolveOutput(average, rule);
        ServerLevel level = player.serverLevel();
        BlockPos pos = pot.getBlockPos();
        CookingDelays.run(rule.delayTicks(), () -> {
            if (level.getBlockEntity(pos) != null
                    && level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null) instanceof IItemHandlerModifiable inventory) {
                inventory.setStackInSlot(0, CookingRecipes.stack(outputId));
            }
        });

        Set<Integer> remainingCategories = new HashSet<>();
        for (InkCategoryGroup group : rule.categoryGroups()) remainingCategories.add(group.category());
        Map<String, Integer> remainingReagents = new HashMap<>();
        for (ItemRequirement reagent : rule.reagents()) remainingReagents.put(reagent.itemId(), reagent.count());

        for (int slot = 0; slot < 9; slot++) {
            ItemStack input = pot.getItem(slot);
            if (input.is(materialTag)) {
                int category = category(input, rule);
                if (category != 0 && remainingCategories.remove(category)) {
                    pot.removeItem(slot, 1);
                }
            } else {
                String identity = BuiltInRegistries.ITEM.getKey(input.getItem()).toString();
                int needed = remainingReagents.getOrDefault(identity, 0);
                if (needed > 0) {
                    int removed = Math.min(input.getCount(), needed);
                    pot.removeItem(slot, removed);
                    remainingReagents.put(identity, needed - removed);
                }
            }
        }
        pot.setCooking(true);
        if (pot.iron()) pot.setCookAnimation(9);
        pot.changed();
        player.closeContainer();
    }

    private static String resolveOutput(double average, InkBlankRule rule) {
        for (InkGradeTier tier : rule.gradeTiers()) {
            if (average >= tier.minAverage()) return tier.outputId();
        }
        return rule.gradeTiers().getLast().outputId();
    }

    private static int category(ItemStack stack, InkBlankRule rule) {
        for (InkCategoryGroup group : rule.categoryGroups()) {
            for (String tagId : group.tags()) {
                if (stack.is(ItemTags.create(ResourceLocation.parse(tagId)))) return group.category();
            }
        }
        return 0;
    }
}
