package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/**
 * 战斗状态记分板：{@code zdztbegin} 置 1 表示进入战斗，{@code zdzttick} 记最近一次被生物伤害的时间。
 * 受击（来源 {@code EntityHurt}）与施放技能（来源 {@code PlayerRightClick}）都会进入战斗状态；
 * 恢复与升级界面读取这两个记分项。
 */
public final class FightingState {
    private FightingState() {
    }

    public static void mark(LivingEntity entity) {
        Scoreboard scoreboard = entity.level().getScoreboard();
        score(scoreboard, "zdztbegin", entity, 1);
        score(scoreboard, "zdzttick", entity, entity.getLastHurtByMobTimestamp());
    }

    /** 元婴出窍耗神不足时，按来源同时清除战斗开关与受击时间。 */
    public static void clear(LivingEntity entity) {
        Scoreboard scoreboard = entity.level().getScoreboard();
        score(scoreboard, "zdztbegin", entity, 0);
        score(scoreboard, "zdzttick", entity, 0);
    }

    private static void score(Scoreboard scoreboard, String name, Entity holder, int value) {
        Objective objective = scoreboard.getObjective(name);
        if (objective == null) {
            objective = scoreboard.addObjective(name, ObjectiveCriteria.DUMMY, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER, true, null);
        }
        scoreboard.getOrCreatePlayerScore(holder, objective).set(value);
    }
}
