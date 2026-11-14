package net.yvanchuxiuzhen.system.creature.api;

import net.minecraft.world.entity.Entity;

/** 副本刷怪时由生物系统接收来源等级与临时攻击者；副本标记和控制点归副本系统。 */
public final class DungeonCreatures {
    private DungeonCreatures() {
    }

    public static void prepare(Entity entity, int level, String attacker) {
        entity.getPersistentData().putInt("LEVELnumber", level);
        if (!attacker.isEmpty()) entity.getPersistentData().putString("linshigongjizhe", attacker);
    }
}
