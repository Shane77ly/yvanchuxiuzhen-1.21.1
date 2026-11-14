package net.yvanchuxiuzhen.system.liandan.api;

import java.util.List;

/** 宝箱丹方的配方说明、等级与有序材料实例；同种材料的重复位置保留。 */
public record MedicineRecipeReward(String name, String description, String medicineDescription,
        String levelText, int level, List<Ingredient> ingredients) {
    public MedicineRecipeReward {
        ingredients = List.copyOf(ingredients);
    }

    public record Ingredient(String id, String name) {
    }
}
