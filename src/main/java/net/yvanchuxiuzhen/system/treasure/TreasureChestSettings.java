package net.yvanchuxiuzhen.system.treasure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** 上锁宝箱首次开箱时的二十三类、八档等级和数量上限，按来源抽样顺序生成。 */
final class TreasureChestSettings {
    private static final String[] CATEGORIES = {"书架", "书架", "书架", "书架", "书架", "书架",
            "药架", "药架", "药架", "药架", "药架", "武器架", "武器架", "武器架", "武器架", "武器架",
            "宝箱", "宝箱", "宝箱", "宝箱", "宝箱", "宝箱", "宝箱"};
    private static final String[] SUBTYPES = {"书籍堆", "书桶", "符箓堆", "丹方", "功法书", "技能书",
            "草药堆", "丹药堆", "恢复类丹药堆", "状态类丹药堆", "精进修为丹药堆", "武器架", "法宝架", "盔甲架", "饰品箱", "装备",
            "妖兽材料", "异火", "金属材料", "灵石", "宝石", "妖丹", "综合"};
    private static final int[] MAXIMA = {1, 1, 2, 1, 1, 1, 3, 2, 2, 2, 2, 1, 1, 1, 1, 1, 5, 5, 5, 3, 3, 2, 5};

    private TreasureChestSettings() {
    }

    static void initialize(CompoundTag data, RandomSource random) {
        int type = Mth.nextInt(random, 1, 23) - 1;
        int roll = Mth.nextInt(random, 1, 100);
        int grade = roll >= 98 ? 8 : roll >= 95 ? 7 : roll >= 90 ? 6 : roll >= 82 ? 5
                : roll >= 65 ? 4 : roll >= 48 ? 3 : roll >= 25 ? 2 : 1;
        data.putDouble("TreasureLEVEL", grade);
        data.putDouble("minimumvalue", 1);
        data.putString("TreasureTypeBig", CATEGORIES[type]);
        data.putString("TreasureTypeSmall", SUBTYPES[type]);
        data.putDouble("Maximumvalue", type >= 16 ? Mth.nextInt(random, 1, MAXIMA[type]) : MAXIMA[type]);
    }
}
