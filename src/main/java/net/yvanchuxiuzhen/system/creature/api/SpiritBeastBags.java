package net.yvanchuxiuzhen.system.creature.api;

import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.system.creature.SpiritBeastBagDeaths;

/** 灵兽袋关联实体死亡的公开写入口；袋实例与召唤物清理由生物系统执行。 */
public final class SpiritBeastBags {
    private SpiritBeastBags() {
    }

    /** 在来源实体的原生死亡处理之后调用，按来源袋UUID重建原槽物品并清理召唤物。 */
    public static void onCreatureDeath(Entity entity) {
        SpiritBeastBagDeaths.afterDeath(entity);
    }
}
