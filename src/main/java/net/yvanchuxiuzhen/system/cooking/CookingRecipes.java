package net.yvanchuxiuzhen.system.cooking;

import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.api.Cooking;
import net.yvanchuxiuzhen.system.cooking.api.CookingRecipeData;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;
import net.yvanchuxiuzhen.system.cooking.data.CookingRuleDefinition.PotContainerRule;
import net.yvanchuxiuzhen.system.cooking.data.CookingTableRecipes;

/** 菜谱逐项检查同物品身份，实际逐项扣料后立即覆盖中槽；盛装容器只检查而不消耗。 */
public final class CookingRecipes {
    private CookingRecipes() {
    }

    static void begin(ServerPlayer player, CookingPotBlockEntity pot) {
        CookingRecipeData recipe = data(pot.getItem(13));
        String type = recipe.potType();
        if (!type.equals(pot.iron() ? "炒锅" : "砂锅")) {
            CookingRuntime.message(player, "§4此菜谱记录的菜肴需要" + type + "烹饪。");
            return;
        }
        if ((int) recipe.grade() > (int) pot.playerCookLevel()) {
            CookingRuntime.message(player, "§4您的厨艺等级并不能支撑您炒完这盘菜！可以先尝试做一些简单的烹饪。");
            return;
        }
        if (!hasContainer(player, pot, recipe.neededContainer())) return;
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.ingredient(ingredient);
            if (!identity.isEmpty() && findIngredient(pot, identity) < 0) {
                CookingRuntime.message(player, "§4请放置菜谱需求的食材！");
                return;
            }
        }
        if (!CookingRuntime.heated(player, pot)) return;
        pot.setCooking(true);
        player.closeContainer();
        pot.setCookAnimation(recipe.animation());
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.ingredient(ingredient);
            if (!identity.isEmpty()) {
                int slot = findIngredient(pot, identity);
                if (slot >= 0) pot.removeItem(slot, 1);
            }
        }
        pot.setItem(4, stack(recipe.outputId().toLowerCase(Locale.ROOT)));
        pot.setCookExp((int) (Math.pow(pot.belongToLevel(), 2) + 5));
        pot.changed();
        CookingRuntime.message(player, "烹饪开始！请不断右键铁锅翻炒食材。");
    }

    private static boolean hasContainer(ServerPlayer player, CookingPotBlockEntity pot, String identity) {
        if (identity.isEmpty() || identity.equals("无")) return true;
        String normalized = normalize(identity);
        for (PotContainerRule rule : CookingTableRecipes.potContainers()) {
            if (normalized.equals(rule.itemId())) {
                if (pot.getItem(rule.slotOffset() + 9).is(stack(rule.itemId()).getItem())) return true;
                CookingRuntime.message(player, rule.potError());
                return false;
            }
        }
        return false;
    }

    private static int findIngredient(CookingPotBlockEntity pot, String identity) {
        for (int slot = 0; slot < 9; slot++) {
            if (BuiltInRegistries.ITEM.getKey(pot.getItem(slot).getItem()).toString().equals(normalize(identity))) return slot;
        }
        return -1;
    }

    public static CookingRecipeData data(ItemStack stack) {
        return Cooking.recipe(stack);
    }

    public static ItemStack stack(String identity) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(normalize(identity))));
    }

    /** 两来源物品在目标中共用命名空间，物品字段的路径与大小写规则保持来源语义。 */
    private static String normalize(String identity) {
        return identity.startsWith("yvanchuxitong:") ? ModId.VALUE + identity.substring("yvanchuxitong".length()) : identity;
    }
}
