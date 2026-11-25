package net.yvanchuxiuzhen.system.creature.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;

/** 悬赏生成前提交妖兽等级与悬赏身份，属性与境界仍由妖兽自己的生成生命周期初始化。 */
public final class CreatureBounties {
    private CreatureBounties() {
    }

    public static void prepare(Mob creature, int level) {
        EntityAttributes.setLevel(creature, level);
    }

    public static boolean isBounty(Entity entity) {
        return entity.getData(CreatureData.BOUNTY_ENTITY);
    }

    public static void setBounty(Entity entity, boolean bounty) {
        entity.setData(CreatureData.BOUNTY_ENTITY, bounty);
    }
}
