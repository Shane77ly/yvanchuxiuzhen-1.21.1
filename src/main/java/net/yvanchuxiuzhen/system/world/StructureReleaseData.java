package net.yvanchuxiuzhen.system.world;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;

/** 由extract_structure_releases.py生成；28个名字的42个模板按锁定来源顺序放置。 */
final class StructureReleaseData {
    private static final List<Placement> PLACEMENTS = List.of(
            new Placement("花繁城", "fanhuacheng01", new BlockPos(-169, 62, -276)),
            new Placement("花繁城", "fanhuacheng02", new BlockPos(-146, 62, -285)),
            new Placement("花繁城", "fanhuacheng03", new BlockPos(-107, 62, -303)),
            new Placement("花繁城", "fanhuacheng04", new BlockPos(-107, 62, -237)),
            new Placement("花繁城", "fanhuacheng05", new BlockPos(-107, 62, -255)),
            new Placement("花繁城", "fanhuacheng06", new BlockPos(-59, 62, -255)),
            new Placement("花繁城", "fanhuacheng07", new BlockPos(-59, 62, -303)),
            new Placement("花繁城", "fanhuacheng08", new BlockPos(-120, 62, -336)),
            new Placement("花繁城", "fanhuacheng09", new BlockPos(-72, 62, -336)),
            new Placement("花繁城", "fanhuacheng10", new BlockPos(-46, 62, -336)),
            new Placement("花繁城", "fanhuacheng11", new BlockPos(-120, 62, -384)),
            new Placement("花繁城", "fanhuacheng12", new BlockPos(-72, 62, -384)),
            new Placement("花繁城", "fanhuacheng13", new BlockPos(-24, 62, -384)),
            new Placement("花繁城", "fanhuacheng14", new BlockPos(-120, 62, -303)),
            new Placement("花繁城", "zhaowunpc", new BlockPos(-811, 63, -217)),
            new Placement("冲关河村", "chongguanhecun", new BlockPos(-688, 59, -859)),
            new Placement("雪地商店", "shoponsnow01", new BlockPos(-324, 70, 981)),
            new Placement("平原马车商店", "shoponplain01", new BlockPos(214, 71, 368)),
            new Placement("水上孤舟", "shoponwater01", new BlockPos(22, 62, 15)),
            new Placement("平原营地商店", "shoponplain02", new BlockPos(-363, 71, 701)),
            new Placement("冰封巨骨", "icejiegou01", new BlockPos(-1315, 71, -464)),
            new Placement("裸露巨骨", "icejiegou02", new BlockPos(885, 69, 1106)),
            new Placement("冰原哨所", "icejiegou03", new BlockPos(-1142, 88, -240)),
            new Placement("半圆冰屋", "icejiegou04", new BlockPos(-475, 77, 77)),
            new Placement("花田巨柳", "flower01", new BlockPos(162, 70, 486)),
            new Placement("马车残骸", "ruins01", new BlockPos(-316, 63, 94)),
            new Placement("炎君传承地", "chuancheng01", new BlockPos(-2741, 87, 1593)),
            new Placement("天狼王传承地", "chuancheng02", new BlockPos(-566, 78, -771)),
            new Placement("空谷残卷其一", "konggucanjvan01", new BlockPos(212, 65, 8)),
            new Placement("空谷残卷其二", "konggucanjvan02", new BlockPos(577, 71, -86)),
            new Placement("空谷残卷其三", "konggucanjvan03", new BlockPos(770, 119, -96)),
            new Placement("空谷残卷其四", "konggucanjvan04", new BlockPos(995, 63, -121)),
            new Placement("空谷残卷其五", "konggucanjvan05", new BlockPos(2945, 75, -143)),
            new Placement("空谷残卷其六", "konggucanjvan06", new BlockPos(1466, 70, -1477)),
            new Placement("空谷残卷其七", "konggucanjvan07", new BlockPos(2420, 78, -1542)),
            new Placement("空谷残卷其八", "konggucanjvan08", new BlockPos(4537, 71, -586)),
            new Placement("镇妖塔", "zhenyaota", new BlockPos(1268, 63, -600)),
            new Placement("云清派", "yvnqingpai", new BlockPos(-882, 141, -267)),
            new Placement("佣兵营地", "yongbingyingdi", new BlockPos(1473, 91, 13)),
            new Placement("丹王葫芦", "danwanghulu", new BlockPos(1544, 73, -1488)),
            new Placement("孤礁小筑", "gujiaoxiaozhu", new BlockPos(1130, 64, -723)),
            new Placement("野外营地", "yewaiyingdi", new BlockPos(591, 63, -1189)));

    private StructureReleaseData() {
    }

    static void place(ServerLevel level, String name) {
        for (Placement placement : PLACEMENTS) {
            if (placement.name().equals(name)) {
                WorldStructures.place(level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, placement.template()), placement.pos());
            }
        }
    }

    private record Placement(String name, String template, BlockPos pos) {
    }
}
