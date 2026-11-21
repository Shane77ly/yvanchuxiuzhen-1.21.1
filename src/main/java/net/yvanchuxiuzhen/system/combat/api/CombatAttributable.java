package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.Entity;

/**
 * 可把伤害实体归因到实际施放者的共享接口。技能实体实现它后，死亡结算无需依赖具体技能类。
 */
public interface CombatAttributable {
    /** 返回造成当前战斗行为的上游实体；没有可解析施放者时返回 {@code null}。 */
    Entity combatAttacker();
}
