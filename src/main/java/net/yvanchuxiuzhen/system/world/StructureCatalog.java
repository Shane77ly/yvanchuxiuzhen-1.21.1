package net.yvanchuxiuzhen.system.world;

import java.util.List;
import net.minecraft.world.phys.Vec3;

/** 每次维度载入都会重设的来源遗迹目录，以固定表保留二十八条记录及其先后顺序。 */
final class StructureCatalog {
    static final List<Entry> ENTRIES = List.of(
            new Entry("花繁城", "huafancheng", -169, 65, -274, "僵尸群"),
            new Entry("冲关河村", "chongguanhecun", -640, 65, -785, "僵尸群"),
            new Entry("雪地商店", "xvedishangdian", -317, 73, 988, "僵尸群"),
            new Entry("平原马车商店", "pingyvanshangdian", 221, 73, 375, "僵尸群"),
            new Entry("水上孤舟", "shuishangguzhou", 28, 64, 22, "无"),
            new Entry("平原营地商店", "pingyvanyingdishangdian", -356, 73, 708, "僵尸群"),
            new Entry("冰封巨骨", "bingfengjvgu", -1304, 73, -458, "小股僵尸"),
            new Entry("裸露巨骨", "luolujvgu", 891, 71, 1109, "小股僵尸"),
            new Entry("冰原哨所", "bingyvanshaosuo", -1136, 90, -234, "小股僵尸"),
            new Entry("半圆冰屋", "banyvanbingwu", -469, 79, 83, "小股僵尸"),
            new Entry("花田巨柳", "huatianjvliu", 167, 72, 492, "无"),
            new Entry("马车残骸", "machecanhai", -310, 65, 100, "小股僵尸"),
            new Entry("炎君传承地", "yanjvnchuanchengdi", -2734, 91, 1600, "无"),
            new Entry("天狼王传承地", "tianlangwangchuanchengdi", -549, 92, -751, "无"),
            new Entry("空谷残卷其一", "konggucanjvanqiyi", 212, 67, 14, "无"),
            new Entry("空谷残卷其二", "konggucanjvanqier", 577, 72, -78, "无"),
            new Entry("空谷残卷其三", "konggucanjvanqisan", 770, 120, -88, "无"),
            new Entry("空谷残卷其四", "konggucanjvanqisi", 996, 64, -115, "无"),
            new Entry("空谷残卷其五", "konggucanjvanqiwu", 2945, 78, -133, "无"),
            new Entry("空谷残卷其六", "konggucanjvanqiliu", 1466, 71, -1465, "无"),
            new Entry("空谷残卷其七", "konggucanjvanqiqi", 2420, 79, -1529, "无"),
            new Entry("空谷残卷其八", "konggucanjvanqiba", 4555, 72, -586, "无"),
            new Entry("镇妖塔", "zhenyaota", 1267, 63, -588, "无"),
            new Entry("云清派", "yvnqingpai", -810, 64, -217, "无"),
            new Entry("佣兵营地", "yongbingyingdi", 1472, 92, 29, "无"),
            new Entry("丹王葫芦", "danwanghulu", 1540, 72, -1483, "无"),
            new Entry("孤礁小筑", "gujiaoxiaozhu", 1156, 65, -702, "无"),
            new Entry("野外营地", "yewaiyingdi", 606, 63, -1190, "无"));

    private StructureCatalog() { }

    record Entry(String name, String id, int x, int y, int z, String mobs) {
        Vec3 position() { return new Vec3(x, y, z); }
    }
}
