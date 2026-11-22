package net.yvanchuxiuzhen.system.lianqi.api;

import java.util.List;

/** 来源奖励图纸的配方描述，九个材料格保持原顺序。 */
public record BlueprintReward(String name, String description, String itemId, String itemDescription,
                              String type, String levelText, int level, List<Ingredient> ingredients) {
    public BlueprintReward {
        ingredients = List.copyOf(ingredients);
    }

    public record Ingredient(String id, String name, int count) {
    }
}
