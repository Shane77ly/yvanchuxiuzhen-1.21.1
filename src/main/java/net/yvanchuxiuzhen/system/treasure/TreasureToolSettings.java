package net.yvanchuxiuzhen.system.treasure;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** 天道宝箱工具的十个来源按钮只修改操作发生时的主手物品，保留独立的客户端显示默认值。 */
public final class TreasureToolSettings {
    private static final List<String> CATEGORIES = List.of("书架", "药架", "武器架", "宝箱", "储物袋", "纳戒");
    private static final List<String> FIRST_SUBTYPES = List.of("书籍堆", "草药堆", "武器架", "普通的箱子", "普通修士储物袋", "普通修士纳戒");
    private static final List<List<String>> SUBTYPES = List.of(
            List.of("书籍堆", "书桶", "符箓堆", "丹方", "炼器", "功法书", "技能书"),
            List.of("草药堆", "丹药堆", "恢复类丹药堆", "状态类丹药堆", "精进修为丹药堆", "采药篓", "种子包"),
            List.of("武器架", "法宝架", "盔甲架", "饰品箱", "综合宝箱"),
            List.of("华丽的箱子", "精致的箱子", "普通的箱子", "破旧的箱子", "建材箱"),
            List.of("炼器师储物袋", "炼丹师储物袋", "画符师储物袋", "御兽师储物袋", "普通修士储物袋", "体修储物袋",
                    "剑修储物袋", "魂修储物袋", "鬼修储物袋", "血修储物袋", "魔修储物袋", "傀儡师储物袋", "机关术师储物袋"),
            List.of("炼器师纳戒", "炼丹师纳戒", "画符师纳戒", "御兽师纳戒", "普通修士纳戒", "体修纳戒",
                    "剑修纳戒", "魂修纳戒", "鬼修纳戒", "血修纳戒", "魔修纳戒", "傀儡师纳戒", "机关术师纳戒"));

    private TreasureToolSettings() {
    }

    public static CompoundTag data(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static boolean action(ItemStack stack, int button) {
        if (button < 0 || button > 9) return false;
        CompoundTag data = data(stack);
        int direction = button % 2 == 0 ? -1 : 1;
        int category = CATEGORIES.indexOf(data.getString("TreasureTypeBig"));
        switch (button / 2) {
            case 0 -> {
                if (category >= 0) {
                    int next = Math.floorMod(category + direction, CATEGORIES.size());
                    data.putString("TreasureTypeBig", CATEGORIES.get(next));
                    data.putString("TreasureTypeSmall", FIRST_SUBTYPES.get(next));
                }
            }
            case 1 -> {
                if (category >= 0) {
                    List<String> values = SUBTYPES.get(category);
                    int subtype = values.indexOf(data.getString("TreasureTypeSmall"));
                    if (subtype >= 0) data.putString("TreasureTypeSmall", values.get(Math.floorMod(subtype + direction, values.size())));
                }
            }
            case 2 -> {
                double value = data.getDouble("TreasureLEVEL") + direction;
                if (direction > 0 && value > 8) value = 1;
                if (direction < 0 && value <= 0) value = 8;
                data.putDouble("TreasureLEVEL", value);
            }
            case 3 -> {
                double value = data.getDouble("minimumvalue") + direction;
                if (direction > 0 && value > 64) value = 1;
                if (direction < 0 && value < 0) value = 64;
                data.putDouble("minimumvalue", value);
                alignMaximum(data);
            }
            case 4 -> {
                double value = data.getDouble("Maximumvalue") + direction;
                if (direction > 0 && value > 64) value = 1;
                if (direction < 0 && value <= 0) value = 64;
                data.putDouble("Maximumvalue", value);
                alignMaximum(data);
            }
            default -> throw new IllegalStateException("未知宝箱设置按钮 " + button);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return true;
    }

    private static void alignMaximum(CompoundTag data) {
        if (data.getDouble("minimumvalue") > data.getDouble("Maximumvalue")) {
            data.putDouble("Maximumvalue", data.getDouble("minimumvalue"));
        }
    }

    /** 来源五个显示过程只在客户端渲染时写默认值；这些值不作为请求传回服务端。 */
    public static CompoundTag display(ItemStack stack) {
        CompoundTag data = data(stack);
        if (data.getString("TreasureTypeBig").isEmpty()) data.putString("TreasureTypeBig", "书架");
        if (data.getString("TreasureTypeSmall").isEmpty()) data.putString("TreasureTypeSmall", "书桶");
        if (data.getDouble("TreasureLEVEL") <= 0) data.putDouble("TreasureLEVEL", 1);
        if (data.getDouble("minimumvalue") <= 0) data.putDouble("minimumvalue", 1);
        if (data.getDouble("Maximumvalue") <= 0) data.putDouble("Maximumvalue", 3);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return data;
    }
}
