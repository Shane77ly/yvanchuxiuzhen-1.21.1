package net.yvanchuxiuzhen.system.cooking;

import java.util.Locale;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockEntity;

/** 菜谱逐项检查同物品身份，实际逐项扣料后立即覆盖中槽；盛装容器只检查而不消耗。 */
public final class CookingRecipes {
    private static final String[] CONTAINERS = {"foodwan", "foodcipan", "fooddawan", "foodtiepan"};
    private static final String[] CONTAINER_ERRORS = {
            "§4需要在左侧四个容器栏位的第一个位置放置一个瓷碗！",
            "§4需要在左侧四个容器栏位的第二个位置放置一个瓷盘！",
            "§4需要在左侧四个容器栏位的第三个位置放置一个大碗！",
            "§4需要在左侧四个容器栏位的第二个位置放置一个铁盘！"};

    private CookingRecipes() {
    }

    static void begin(ServerPlayer player, CookingPotBlockEntity pot) {
        CompoundTag recipe = data(pot.getItem(13));
        String type = recipe.getString("MenuType");
        if (!type.equals(pot.iron() ? "炒锅" : "砂锅")) {
            CookingRuntime.message(player, "§4此菜谱记录的菜肴需要" + type + "烹饪。");
            return;
        }
        CompoundTag state = pot.getPersistentData();
        if (recipe.getInt("belongTopinjie") > state.getInt("playercooklevel")) {
            CookingRuntime.message(player, "§4您的厨艺等级并不能支撑您炒完这盘菜！可以先尝试做一些简单的烹饪。");
            return;
        }
        if (!hasContainer(player, pot, recipe.getString("needcontainer"))) return;
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.getString("cailiao0" + ingredient);
            if (!identity.isEmpty() && findIngredient(pot, identity) < 0) {
                CookingRuntime.message(player, "§4请放置菜谱需求的食材！");
                return;
            }
        }
        if (!CookingRuntime.heated(player, pot)) return;
        state.putBoolean("cooking", true);
        player.closeContainer();
        state.putInt("cookanimation", recipe.getInt("cookanimation"));
        for (int ingredient = 0; ingredient < 9; ingredient++) {
            String identity = recipe.getString("cailiao0" + ingredient);
            if (!identity.isEmpty()) {
                int slot = findIngredient(pot, identity);
                if (slot >= 0) pot.removeItem(slot, 1);
            }
        }
        pot.setItem(4, stack(recipe.getString("Menuid").toLowerCase(Locale.ROOT)));
        state.putInt("cookexp", (int) (Math.pow(state.getInt("belongToLevel"), 2) + 5));
        pot.changed();
        CookingRuntime.message(player, "烹饪开始！请不断右键铁锅翻炒食材。");
    }

    private static boolean hasContainer(ServerPlayer player, CookingPotBlockEntity pot, String identity) {
        if (identity.isEmpty() || identity.equals("无")) return true;
        for (int index = 0; index < CONTAINERS.length; index++) {
            if (normalize(identity).equals(ModId.VALUE + ":" + CONTAINERS[index])) {
                if (pot.getItem(index + 9).is(stack(identity).getItem())) return true;
                CookingRuntime.message(player, CONTAINER_ERRORS[index]);
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

    public static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static ItemStack stack(String identity) {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(normalize(identity))));
    }

    /** 两来源物品在目标中共用命名空间，物品字段的路径与大小写规则保持来源语义。 */
    private static String normalize(String identity) {
        return identity.startsWith("yvanchuxitong:") ? ModId.VALUE + identity.substring("yvanchuxitong".length()) : identity;
    }
}
