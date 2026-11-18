package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * 战斗状态记分板：{@code zdztbegin} 置 1 表示进入战斗，{@code zdzttick} 由上次受击时间初始化并在战斗中逐刻递增。
 * 受击（来源 {@code EntityHurt}）与施放技能（来源 {@code PlayerRightClick}）都会进入战斗状态；
 * 恢复与升级界面读取这两个记分项。
 */
public final class FightingState {
    private static final String ACTIVE = "zdztbegin";
    private static final String TICKS = "zdzttick";

    private FightingState() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(FightingState::onEntityTick);
        NeoForge.EVENT_BUS.addListener(FightingState::onPlayerTick);
    }

    public static void mark(LivingEntity entity) {
        Scoreboard scoreboard = entity.level().getScoreboard();
        score(scoreboard, ACTIVE, entity, 1);
        score(scoreboard, TICKS, entity, entity.getLastHurtByMobTimestamp());
    }

    /** 战斗超时或元婴出窍耗神不足时，同时清除战斗开关与计数。 */
    public static void clear(LivingEntity entity) {
        Scoreboard scoreboard = entity.level().getScoreboard();
        score(scoreboard, ACTIVE, entity, 0);
        score(scoreboard, TICKS, entity, 0);
    }

    private static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity && !(entity instanceof Player) && !entity.level().isClientSide()) {
            tick(entity);
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) tick(player);
    }

    /** 来源 EntityTick：先推进保存的计数，再与实体当前受击时间比较。 */
    private static void tick(LivingEntity entity) {
        Scoreboard scoreboard = entity.level().getScoreboard();
        if (read(scoreboard, ACTIVE, entity) != 1) return;
        int next = read(scoreboard, TICKS, entity) + 1;
        score(scoreboard, TICKS, entity, next);
        if ((long) next - entity.getLastHurtByMobTimestamp() >= 100) {
            clear(entity);
        }
    }

    private static int read(Scoreboard scoreboard, String name, Entity holder) {
        Objective objective = scoreboard.getObjective(name);
        ReadOnlyScoreInfo value = objective == null ? null : scoreboard.getPlayerScoreInfo(holder, objective);
        return value == null ? 0 : value.value();
    }

    private static void score(Scoreboard scoreboard, String name, Entity holder, int value) {
        Objective objective = scoreboard.getObjective(name);
        if (objective == null) {
            objective = scoreboard.addObjective(name, ObjectiveCriteria.DUMMY, Component.literal(name), ObjectiveCriteria.RenderType.INTEGER, true, null);
        }
        scoreboard.getOrCreatePlayerScore(holder, objective).set(value);
    }
}
