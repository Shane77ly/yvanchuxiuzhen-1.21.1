package net.yvanchuxiuzhen.system.craft;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.CarpentryRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftTableRecipes;

/** 木匠台从 JSON 配方表按类别读取来源顺序与材料成本，单次制作一件。 */
public final class CarpentryRecipes {
    public static final String BEECH = "榉木";
    public static final String MISCELLANEOUS = "杂物";

    private CarpentryRecipes() {
    }

    public static List<Recipe> category(String category) {
        return CraftTableRecipes.carpentry(category).stream()
                .map(CarpentryRecipes::toRecipe)
                .toList();
    }

    private static Recipe toRecipe(CarpentryRecipe definition) {
        List<Cost> costs = definition.costs().stream()
                .map(c -> new Cost(c.itemId(), c.count()))
                .toList();
        return new Recipe(definition.outputId(), costs);
    }

    public record Recipe(String itemId, List<Cost> costs) {
        public Item item() {
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        }
    }

    public record Cost(String itemId, int count) {
        public Item item() {
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        }
    }
}
