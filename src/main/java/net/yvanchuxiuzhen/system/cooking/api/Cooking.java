package net.yvanchuxiuzhen.system.cooking.api;

import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.cooking.data.CookingData;

/** 烹饪系统的酒类与菜谱物品组件公开读写入口。 */
public final class Cooking {
    private Cooking() {
    }

    public static double wineAge(ItemStack stack) {
        return stack.getOrDefault(CookingData.WINE.get(), WineData.EMPTY).ageYears();
    }

    public static void setWineAge(ItemStack stack, double ageYears) {
        stack.set(CookingData.WINE.get(), new WineData(ageYears));
    }

    public static CookingRecipeData recipe(ItemStack stack) {
        return stack.getOrDefault(CookingData.RECIPE.get(), CookingRecipeData.EMPTY);
    }

    public static void setRecipe(ItemStack stack, CookingRecipeData recipe) {
        stack.set(CookingData.RECIPE.get(), recipe);
    }
}
