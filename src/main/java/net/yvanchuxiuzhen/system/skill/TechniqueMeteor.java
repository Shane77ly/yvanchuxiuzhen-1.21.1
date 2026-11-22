package net.yvanchuxiuzhen.system.skill;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.system.combat.api.TechniqueMeteorEvent;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;

/** 功法命中触发的天外陨石，来源没有设置攻击者 UUID。 */
final class TechniqueMeteor {
    private TechniqueMeteor() { }

    static void spawn(TechniqueMeteorEvent event) {
        ServerLevel level = event.player().serverLevel();
        SkillEntity meteor = SkillSetup.<SkillEntity>entityType("skill_rock_03").get().create(level);
        meteor.moveTo(event.victim().getX(), event.victim().getY() + 16, event.victim().getZ(),
                level.getRandom().nextFloat() * 360F, 0);
        Relations.shareEntityAllegiance(event.player(), meteor);
        meteor.configure((UUID) null, 1, event.multiplier(), "土", "天外陨石");
        SkillEntities.spawn(level, meteor, true);
    }
}
