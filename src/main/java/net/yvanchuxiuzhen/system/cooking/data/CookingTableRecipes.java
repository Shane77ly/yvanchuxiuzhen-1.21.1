package net.yvanchuxiuzhen.system.cooking.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.InkBlankRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.MealPlateRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.PotContainerRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.StillBrewingRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.WineDrinkRule;

/** 加载并索引 {@code content/cooking/recipes.json} 中的烹饪与酿酒数据规则。 */
public final class CookingTableRecipes {
    private static final String PATH = "content/cooking/recipes.json";
    private static volatile Table table;

    private CookingTableRecipes() {
    }

    public static List<StillBrewingRule> stillBrewing() {
        return table().stillBrewing();
    }

    public static Optional<WineDrinkRule> wineDrink(String wineId) {
        return Optional.ofNullable(table().wineDrinks().get(wineId));
    }

    public static InkBlankRule inkBlank() {
        return table().inkBlank();
    }

    public static List<MealPlateRule> mealPlates() {
        return table().mealPlates();
    }

    public static List<PotContainerRule> potContainers() {
        return table().potContainers();
    }

    private static Table table() {
        Table loaded = table;
        if (loaded == null) {
            List<StillBrewingRule> still = new ArrayList<>();
            Map<String, WineDrinkRule> wines = new LinkedHashMap<>();
            InkBlankRule ink = null;
            List<MealPlateRule> meals = new ArrayList<>();
            List<PotContainerRule> containers = new ArrayList<>();
            for (CookingRuleDefinition entry : ContentFiles.read(PATH, CookingRuleDefinition.CODEC)) {
                switch (entry) {
                    case StillBrewingRule rule -> still.add(rule);
                    case WineDrinkRule rule -> wines.put(rule.wineId(), rule);
                    case InkBlankRule rule -> ink = rule;
                    case MealPlateRule rule -> meals.add(rule);
                    case PotContainerRule rule -> containers.add(rule);
                }
            }
            if (ink == null) throw new IllegalStateException("缺少 ink_blank 烹饪规则: " + PATH);
            loaded = new Table(List.copyOf(still), Map.copyOf(wines), ink, List.copyOf(meals), List.copyOf(containers));
            table = loaded;
        }
        return loaded;
    }

    private record Table(
            List<StillBrewingRule> stillBrewing,
            Map<String, WineDrinkRule> wineDrinks,
            InkBlankRule inkBlank,
            List<MealPlateRule> mealPlates,
            List<PotContainerRule> potContainers) {
    }
}
