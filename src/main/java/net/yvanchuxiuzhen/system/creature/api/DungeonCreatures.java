package net.yvanchuxiuzhen.system.creature.api;

import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 副本刷怪时由生物系统接收来源等级与临时攻击者，并提供副本实体身份标记。 */
public final class DungeonCreatures {
    private DungeonCreatures() {
    }

    public static void prepare(Entity entity, int level, String attacker) {
        EntityAttributes.setLevel(entity, level);
        if (!attacker.isEmpty()) CreatureData.state(entity).setTempAttackerUuid(attacker);
    }

    public static boolean isDungeonEntity(Entity entity) {
        return entity.getData(CreatureData.DUNGEON_ENTITY);
    }

    public static void setDungeonEntity(Entity entity, boolean value) {
        entity.setData(CreatureData.DUNGEON_ENTITY, value);
    }
}
