package net.yvanchuxiuzhen.system.economy.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.book.api.BookTextData;
import net.yvanchuxiuzhen.system.book.api.BookTexts;
import net.yvanchuxiuzhen.system.cooking.api.Cooking;
import net.yvanchuxiuzhen.system.fulu.api.Fulu;
import net.yvanchuxiuzhen.system.fulu.api.FuluLearningBook;
import net.yvanchuxiuzhen.system.fulu.api.TalismanInstance;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.liandan.api.Liandan;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipe;
import net.yvanchuxiuzhen.system.liandan.api.MedicineReward;
import net.yvanchuxiuzhen.system.lianqi.api.Lianqi;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;
import net.yvanchuxiuzhen.system.material.api.Materials;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.treasure.api.Caskets;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainerConfig;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/**
 * 强类型商品与任务奖励物品构造工厂，统一由各系统公开组件 API 组装 {@link ItemStack}。
 * 不读写 {@code CUSTOM_DATA}，也不解析 {@code %}/{@code /} 字符串。
 */
public final class EconomyItems {
    private EconomyItems() {
    }

    public static ItemStack create(ShopCatalogDefinition.Product product) {
        return create(product.item(), 1, product.displayName(), Optional.empty(), product.payload());
    }

    public static ItemStack create(ResourceLocation itemId,
                                   int count,
                                   Optional<String> displayName,
                                   Optional<String> colorCode,
                                   ShopCatalogDefinition.ItemPayload payload) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        int stackCount = Math.max(1, count);
        ItemStack result = switch (payload) {
            case ShopCatalogDefinition.NoPayload ignored -> new ItemStack(item, stackCount);
            case ShopCatalogDefinition.MaterialPayload material -> {
                ItemStack stack = new ItemStack(item, stackCount);
                Materials.setInstance(stack, material.level(), material.quality());
                yield stack;
            }
            case ShopCatalogDefinition.TechniquePayload technique -> {
                ItemStack stack = Techniques.createBook(technique.definition());
                stack.setCount(stackCount);
                yield stack;
            }
            case ShopCatalogDefinition.SkillPayload skill -> {
                SkillBook book = new SkillBook(skill.skillKind(), skill.name(), skill.grade(), skill.level(),
                        skill.body(), skill.intro(), Optional.empty(), "", skill.numbers(), skill.texts());
                ItemStack stack = Skills.createBook(item, book);
                stack.setCount(stackCount);
                yield stack;
            }
            case ShopCatalogDefinition.FuluPayload fulu -> {
                ItemStack stack = new ItemStack(item, stackCount);
                Fulu.setTalisman(stack, new TalismanInstance(fulu.level(), fulu.levelText(), 0, 0));
                yield stack;
            }
            case ShopCatalogDefinition.FuluLearningPayload learning -> {
                ItemStack stack = new ItemStack(item, stackCount);
                if (learning.learnable()) Fulu.setLearningBook(stack, new FuluLearningBook(true, learning.command()));
                yield stack;
            }
            case ShopCatalogDefinition.MedicinePayload medicine -> {
                ItemStack stack = Liandan.createMedicineReward(item, new MedicineReward(
                        medicine.name(), medicine.packCount(), medicine.dose(), medicine.treasure(), medicine.effects()));
                stack.setCount(stackCount);
                yield stack;
            }
            case ShopCatalogDefinition.RecipePayload recipe -> {
                ItemStack stack = new ItemStack(item, stackCount);
                List<MedicineRecipe.Ingredient> padded = new ArrayList<>(8);
                for (int slot = 0; slot < 8; slot++) {
                    padded.add(slot < recipe.ingredients().size()
                            ? recipe.ingredients().get(slot)
                            : MedicineRecipe.Ingredient.EMPTY);
                }
                Liandan.setRecipe(stack, new MedicineRecipe(recipe.medicineName(), recipe.recipeDescription(),
                        recipe.medicineDescription(), recipe.levelText(), recipe.level(), recipe.recipeId(), padded));
                yield stack;
            }
            case ShopCatalogDefinition.BlueprintPayload blueprint -> {
                ItemStack stack = new ItemStack(item, stackCount);
                List<LianqiBlueprint.Ingredient> padded = new ArrayList<>(9);
                for (int slot = 0; slot < 9; slot++) {
                    padded.add(slot < blueprint.ingredients().size()
                            ? blueprint.ingredients().get(slot)
                            : LianqiBlueprint.Ingredient.EMPTY);
                }
                Lianqi.setBlueprint(stack, new LianqiBlueprint(blueprint.blueprintName(), "",
                        blueprint.blueprintDescription(), blueprint.artifactId(), blueprint.artifactDescription(),
                        "", blueprint.artifactType(), blueprint.levelText(), blueprint.level(),
                        blueprint.belongToItemId(), padded));
                yield stack;
            }
            case ShopCatalogDefinition.CookingRecipePayload cooking -> {
                ItemStack stack = new ItemStack(item, stackCount);
                Cooking.setRecipe(stack, cooking.recipe());
                yield stack;
            }
            case ShopCatalogDefinition.BookTextPayload bookText -> {
                ItemStack stack = new ItemStack(item, stackCount);
                BookTexts.set(stack, new BookTextData(bookText.text(), List.of()));
                yield stack;
            }
            case ShopCatalogDefinition.TreasureContainerPayload container -> {
                ItemStack stack = new ItemStack(item, stackCount);
                TreasureContainers.setConfig(stack, new TreasureContainerConfig(container.category(),
                        container.subtype(), container.level(), container.minimum(), container.maximum()));
                yield stack;
            }
            case ShopCatalogDefinition.CasketCommandPayload casket -> {
                ItemStack stack = Caskets.create(casket.command());
                stack.setCount(stackCount);
                yield stack;
            }
        };
        if (displayName.isPresent() && !displayName.get().isEmpty() && !displayName.get().equals("无")) {
            String prefix = colorCode.filter(code -> !code.isEmpty()).map(code -> "§" + code).orElse("");
            result.set(DataComponents.CUSTOM_NAME, Component.literal(prefix + displayName.get()));
        }
        return result;
    }
}
