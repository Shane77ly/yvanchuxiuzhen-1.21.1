package net.yvanchuxiuzhen.system.npc.entity;

import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/** 固定商人与大管家的十一枚来源刷怪蛋，颜色与实体绑定逐项保留。 */
final class NpcMerchantSpawnEggs {
    private static final Egg[] EGGS = {
            new Egg("businessman_01", -3368704, -13312),
            new Egg("businessman_02", -16750900, -205),
            new Egg("businessman_03", -6724096, -13312),
            new Egg("businessman_04", -13057, -1),
            new Egg("businessman_05", -4992426, -3878456),
            new Egg("businessman_06", -1, -1),
            new Egg("businessman_07", -6724096, -3167623),
            new Egg("daguanjia", -3368449, -10079488),
            new Egg("businessman_09", -1, -1),
            new Egg("businessman_10", -1, -1),
            new Egg("businessman_11", -1, -1)
    };

    private NpcMerchantSpawnEggs() {
    }

    static void register(ContentTables content) {
        for (Egg egg : EGGS) {
            content.itemFactory("npc:spawn_egg_" + egg.id(), (properties, tooltip) -> new DeferredSpawnEggItem(
                    content.entityType(egg.id()), egg.baseColor(), egg.spotColor(), properties));
        }
    }

    private record Egg(String id, int baseColor, int spotColor) {
    }
}
