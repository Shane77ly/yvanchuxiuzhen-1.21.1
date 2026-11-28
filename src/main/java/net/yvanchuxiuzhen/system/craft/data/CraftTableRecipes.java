package net.yvanchuxiuzhen.system.craft.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.CarpentryRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.GrindingRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.LoomBasicRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.LoomCloudRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.LoomPairRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.PotteryRecipe;
import net.yvanchuxiuzhen.system.craft.data.CraftRecipeDefinition.SilkReelRecipe;

/** 加载并索引 {@code content/craft/recipes.json} 中的木工、石磨、缫丝、织布与陶轮配方。 */
public final class CraftTableRecipes {
    private static final String PATH = "content/craft/recipes.json";
    private static volatile Table table;

    private CraftTableRecipes() {
    }

    public static List<CarpentryRecipe> carpentry(String category) {
        return table().carpentryByCategory().getOrDefault(category, List.of());
    }

    public static List<GrindingRecipe> grinding() {
        return table().grinding();
    }

    public static List<SilkReelRecipe> silkReel() {
        return table().silkReel();
    }

    public static List<LoomBasicRecipe> loomBasic() {
        return table().loomBasic();
    }

    public static List<LoomPairRecipe> loomPairs() {
        return table().loomPairs();
    }

    public static List<LoomCloudRecipe> loomCloud() {
        return table().loomCloud();
    }

    public static List<PotteryRecipe> pottery() {
        return table().pottery();
    }

    private static Table table() {
        Table loaded = table;
        if (loaded == null) {
            Map<String, List<CarpentryRecipe>> carpentry = new LinkedHashMap<>();
            List<GrindingRecipe> grinding = new ArrayList<>();
            List<SilkReelRecipe> silkReel = new ArrayList<>();
            List<LoomBasicRecipe> loomBasic = new ArrayList<>();
            List<LoomPairRecipe> loomPairs = new ArrayList<>();
            List<LoomCloudRecipe> loomCloud = new ArrayList<>();
            List<PotteryRecipe> pottery = new ArrayList<>();
            for (CraftRecipeDefinition entry : ContentFiles.read(PATH, CraftRecipeDefinition.CODEC)) {
                switch (entry) {
                    case CarpentryRecipe recipe -> carpentry.computeIfAbsent(recipe.category(), k -> new ArrayList<>()).add(recipe);
                    case GrindingRecipe recipe -> grinding.add(recipe);
                    case SilkReelRecipe recipe -> silkReel.add(recipe);
                    case LoomBasicRecipe recipe -> loomBasic.add(recipe);
                    case LoomPairRecipe recipe -> loomPairs.add(recipe);
                    case LoomCloudRecipe recipe -> loomCloud.add(recipe);
                    case PotteryRecipe recipe -> pottery.add(recipe);
                }
            }
            Map<String, List<CarpentryRecipe>> immutableCarpentry = new LinkedHashMap<>();
            carpentry.forEach((k, v) -> immutableCarpentry.put(k, List.copyOf(v)));
            loaded = new Table(
                    Map.copyOf(immutableCarpentry),
                    List.copyOf(grinding),
                    List.copyOf(silkReel),
                    List.copyOf(loomBasic),
                    List.copyOf(loomPairs),
                    List.copyOf(loomCloud),
                    List.copyOf(pottery));
            table = loaded;
        }
        return loaded;
    }

    private record Table(
            Map<String, List<CarpentryRecipe>> carpentryByCategory,
            List<GrindingRecipe> grinding,
            List<SilkReelRecipe> silkReel,
            List<LoomBasicRecipe> loomBasic,
            List<LoomPairRecipe> loomPairs,
            List<LoomCloudRecipe> loomCloud,
            List<PotteryRecipe> pottery) {
    }
}
