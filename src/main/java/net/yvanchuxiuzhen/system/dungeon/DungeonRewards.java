package net.yvanchuxiuzhen.system.dungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.BlueprintPayload;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.DungeonRewardEntry;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.ItemPayload;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.MedicineRecipePayload;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.SkillPayload;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.TreasurePayload;
import net.yvanchuxiuzhen.system.lianqi.api.BlueprintReward;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipeReward;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/** 副本强类型奖励条目转实物堆叠，业务实例交由各数据所有者构造。 */
public final class DungeonRewards {
    private DungeonRewards() {
    }

    public static ItemStack create(DungeonRewardEntry entry) {
        Item item = BuiltInRegistries.ITEM.get(entry.itemId());
        ItemStack stack = switch (entry.payload()) {
            case SkillPayload skill -> Skills.createBook(item, new SkillBook(
                    SkillKind.SPELL, skill.name(), skill.grade(), skill.level(), skill.body(),
                    skill.intro(), Optional.empty(), "", skill.numbers(), skill.texts()));
            case BlueprintPayload blueprint -> {
                List<BlueprintReward.Ingredient> ingredients = new ArrayList<>(9);
                for (int i = 0; i < 9; i++) {
                    if (i < blueprint.ingredients().size()) {
                        var ing = blueprint.ingredients().get(i);
                        ingredients.add(new BlueprintReward.Ingredient(ing.id(), ing.name(), ing.count()));
                    } else {
                        ingredients.add(new BlueprintReward.Ingredient("", "", 0));
                    }
                }
                yield Lianqi.createBlueprintReward(item, new BlueprintReward(
                        blueprint.name(), blueprint.description(), blueprint.outputItemId(),
                        blueprint.outputDescription(), blueprint.outputType(), blueprint.itemLevelText(),
                        blueprint.itemLevelNum(), ingredients));
            }
            case MedicineRecipePayload recipe -> {
                List<MedicineRecipeReward.Ingredient> ingredients = new ArrayList<>(8);
                for (int i = 0; i < 8; i++) {
                    if (i < recipe.ingredients().size()) {
                        var ing = recipe.ingredients().get(i);
                        ingredients.add(new MedicineRecipeReward.Ingredient(ing.id(), ing.name()));
                    } else {
                        ingredients.add(new MedicineRecipeReward.Ingredient("", ""));
                    }
                }
                yield Liandan.createRecipeReward(item, new MedicineRecipeReward(
                        recipe.name(), recipe.recipeDescription(), recipe.medicineDescription(),
                        recipe.itemLevelText(), recipe.itemLevelNum(), ingredients));
            }
            case TreasurePayload treasure -> TreasureContainers.create(
                    item, treasure.category(), treasure.subtype(), treasure.level(), treasure.minimum(), treasure.maximum());
            case ItemPayload payload -> {
                ItemStack result = new ItemStack(item);
                if (payload.materialLevel() != 0 || payload.materialGrade() != 0) {
                    Materials.setInstance(result, payload.materialLevel(), payload.materialGrade());
                }
                yield result;
            }
        };
        if (!entry.customName().isEmpty() && !entry.customName().equals("无")) {
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(entry.customName()));
        }
        stack.setCount(entry.count());
        return stack;
    }
}
