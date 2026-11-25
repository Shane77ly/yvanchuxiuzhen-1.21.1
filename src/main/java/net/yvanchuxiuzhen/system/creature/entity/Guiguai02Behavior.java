package net.yvanchuxiuzhen.system.creature.entity;

import java.util.Comparator;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouShared;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 鬼火来源初始化、目标选择、靠近与普攻阶段。 */
final class Guiguai02Behavior {
    private Guiguai02Behavior() { }

    static void initialize(Guiguai02Entity entity) {
        CreatureSourceStats.initialize(entity, 1, false, false,
                "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "强筋");
    }

    static void touch(Guiguai02Entity entity, Player player) {
        CreatureState state = CreatureData.state(entity);
        state.setAttackerUuid(player.getStringUUID());
        state.setSkill("普攻");
    }

    static void hurt(Guiguai02Entity entity, Entity attacker) {
        if (attacker == null || !entity.isAlive() || Relations.isFriendly(entity, attacker)) return;
        CreatureState state = CreatureData.state(entity);
        state.setTempAttackerUuid(attacker.getStringUUID());
        if (state.interrupted()) state.setInterrupted(false);
    }

    static void tick(Guiguai02Entity entity) {
        if (!entity.isAlive()) return;
        CreatureState state = CreatureData.state(entity);
        if (!entity.level().isClientSide()) {
            var objective = entity.level().getScoreboard().getObjective("zhaohuanwu");
            if (objective != null && entity.level().getScoreboard().getOrCreatePlayerScore(entity, objective).get() == 1) {
                double elapsed = state.countdown() + 1;
                if (elapsed >= 100) {
                    state.setSkill("普攻");
                    elapsed = 0;
                }
                state.setCountdown(elapsed);
            }
        }
        state.setTick(state.tick() + 1);
        Vec3 center = entity.position();
        List<Entity> nearby = entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(8), candidate -> true)
                .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList();
        for (Entity candidate : nearby) {
            if (!Relations.isFriendly(entity, candidate)) {
                state.setTempAttackerUuid(candidate.getStringUUID());
                break;
            }
        }
        if (state.skill().isEmpty() && !state.attackerUuid().equals(state.tempAttackerUuid()))
            state.setAttackerUuid(state.tempAttackerUuid());

        Entity target = GuiguaiTargeting.target(entity.level(), state.attackerUuid());
        if (target == null) {
            if (state.tick() >= 40) {
                entity.getNavigation().moveTo(entity.getX() + GuiguaiTargeting.random(-3, 3), entity.getY(),
                        entity.getZ() + GuiguaiTargeting.random(-3, 3), 1);
                state.setTick(0);
            }
            return;
        }
        if (!target.isAlive()) {
            state.setAttackerUuid("");
            state.setTempAttackerUuid("");
            YaoshouShared.finishSkill(entity);
            return;
        }
        if (entity.level().isClientSide()) return;
        if (state.skill().isEmpty()) {
            chooseSkill(entity, target);
            return;
        }
        switch (state.skill()) {
            case "靠近" -> approach(entity, target);
            case "挣脱" -> YaoshouShared.escape(entity);
            case "普攻" -> attack(entity, target);
            default -> { }
        }
    }

    private static void chooseSkill(Guiguai02Entity entity, Entity target) {
        CreatureState state = CreatureData.state(entity);
        if (!state.queuedSkill().isEmpty()) {
            if (state.tick() >= state.queuedTimer()) {
                state.setSkill(state.queuedSkill());
                state.setQueuedSkill("");
                state.setQueuedTimer(0);
                state.setTick(0);
                clearCandidates(state);
            }
            return;
        }
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2, 0));
        double distance = GuiguaiTargeting.surfaceDistance(entity, target);
        state.setCandidateSkill(1, "靠近");
        state.setCandidateTimer(1, 10);
        state.setCandidateWeight(1, 20);
        int count = 1;
        int weight = 20;
        if (distance <= 2) {
            count = 2;
            weight += 100;
            state.setCandidateSkill(2, "普攻");
            state.setCandidateTimer(2, 10);
            state.setCandidateWeight(2, 100);
        }
        state.setCandidateCount(count);
        int roll = GuiguaiTargeting.random(1, weight);
        String selected = roll <= 20 ? "靠近" : "普攻";
        state.setQueuedSkill(selected);
        state.setQueuedTimer(10);
    }

    private static void clearCandidates(CreatureState state) {
        int count = (int) state.candidateCount();
        for (int i = 1; i <= count; i++) {
            state.clearCandidate(i);
        }
        state.setCandidateCount(0);
    }

    private static void approach(Guiguai02Entity entity, Entity target) {
        CreatureState state = CreatureData.state(entity);
        if (state.phase().isEmpty()) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2, 0));
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            state.setPhase("移动中");
        } else if (state.phase().equals("移动中") && state.tick() >= 40) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void attack(Guiguai02Entity entity, Entity target) {
        CreatureState state = CreatureData.state(entity);
        if (!state.phase().equals("准备")) {
            state.setPhase("准备");
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2, 0));
            return;
        }
        if (state.tick() < 1 || !(entity.level() instanceof ServerLevel level)) return;
        level.sendParticles(Particles.type("cailiao_238particle"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                entity.getZ(), 5, 0.2, 0.2, 0.2, 0.1);
        level.sendParticles(Particles.type("brokenskyblue"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                entity.getZ(), 5, 0, 0, 0, 0.5);
        Vec3 center = entity.position();
        List<Entity> nearby = level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1.5), candidate -> true)
                .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList();
        for (Entity candidate : nearby) {
            if (!Relations.isFriendly(entity, candidate)) {
                level.sendParticles(Particles.type("smallexplosionskyblue"), candidate.getX(), candidate.getY() + candidate.getBbHeight() / 2,
                        candidate.getZ(), 1, 0, 0, 0, 0);
                if (candidate instanceof LivingEntity living)
                    SpellDamage.deal(level, living, DamageCategory.FIRE, entity, false, 1, 3);
            }
        }
        entity.discard();
    }
}
