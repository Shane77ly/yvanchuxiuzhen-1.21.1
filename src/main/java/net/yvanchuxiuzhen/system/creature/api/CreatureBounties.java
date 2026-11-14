package net.yvanchuxiuzhen.system.creature.api;

import net.minecraft.world.entity.Mob;

/** 悬赏生成前提交妖兽等级，属性与境界仍由妖兽自己的生成生命周期初始化。 */
public final class CreatureBounties {
    private CreatureBounties() {
    }

    public static void prepare(Mob creature, int level) {
        creature.getPersistentData().putInt("LEVELnumber", level);
    }
}
