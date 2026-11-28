package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Random;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

/** 020号妖兽的分身、临时召唤寿命及来源技能选择。 */
public final class Yaoshou020Behavior {
    private Yaoshou020Behavior() { }

    public static void hurt(YaoshouEntity entity, DamageSource source) {
        Entity attacker = source.getEntity();
        if (entity.isAlive() && attacker != null && !YaoshouShared.isFriendly(entity, attacker)) {
            CreatureState data = CreatureData.state(entity);
            data.setTempAttackerUuid(attacker.getStringUUID());
            if (data.interrupted()) {
                entity.setAnimation("empty");
                data.setInterrupted(false);
            }
            if (data.skill().isEmpty() && !entity.level().isClientSide()) {
                entity.setAnimation("animation.yaoshou020.attacked");
            }
        }
    }

    public static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) return;
        CreatureState data = CreatureData.state(entity);
        if (Yaoshou037054Support.isSummoned(entity)) {
            if (data.countdown() >= 1) {
                data.setCountdown(data.countdown() - 1);
            } else {
                if (entity.level() instanceof ServerLevel level) {
                    level.sendParticles(Particles.type("brokendeeporange"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                            entity.getZ(), 24, 0, 0, 0, 1);
                    entity.discard();
                }
            }
        }
        data.setTick(data.tick() + 1);
        if (data.skill().isEmpty()
                && !data.attackerUuid().equals(data.tempAttackerUuid())) {
            data.setAttackerUuid(data.tempAttackerUuid());
        }
        Entity target = Yaoshou037054Support.target(entity.level(), entity);
        if (target == null) {
            if (data.tick() >= 100) {
                entity.getNavigation().moveTo(entity.getX() + random(-3, 3), entity.getY(), entity.getZ() + random(-3, 3), 1);
                data.setTick(0);
            }
        } else if (!target.isAlive()) {
            data.setAttackerUuid("");
            data.setTempAttackerUuid("");
            YaoshouShared.finishSkill(entity);
        } else if (data.skill().isEmpty()) {
            if (data.queuedSkill().isEmpty()) {
                schedule(entity, target);
            } else if (data.tick() >= data.queuedTimer()) {
                data.setSkill(data.queuedSkill());
                data.setQueuedSkill("");
                data.setQueuedTimer(0);
                data.setTick(0);
                for (int i = 1; i <= data.candidateCount(); i++) {
                    data.setCandidateSkill((int) (i), "");
                    data.setCandidateTimer((int) (i), 0);
                    data.setCandidateWeight((int) (i), 0);
                }
                data.setCandidateCount(0);
            }
        } else {
            switch (data.skill()) {
                case "靠近" -> Yaoshou019Behavior.approach(entity, target);
                case "挣脱" -> YaoshouShared.escape(entity);
                case "普攻" -> Yaoshou019Behavior.attack(entity, target, "animation.yaoshou020.attack");
                case "分身" -> duplicate(entity, target);
                case "逃跑" -> Yaoshou019Behavior.flee(entity, target, "smokedeeporange");
                default -> { }
            }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        double distance = entity.position().distanceTo(target.position()) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) {
            candidate(data, ++count, "挣脱", 10, 100);
        } else {
            boolean accelerated = entity.hasEffect(MobEffects.MOVEMENT_SPEED);
            if (!StatusEffects.has(entity, "effect_002") && !StatusEffects.has(entity, "effect_033")) {
                if (!accelerated) candidate(data, ++count, "分身", 10, 20);
                if (distance > 3) {
                    candidate(data, ++count, "逃跑", 5, 30);
                    if (!accelerated) candidate(data, ++count, "靠近", 5, 10);
                } else {
                    if (!accelerated) candidate(data, ++count, "普攻", 10, 20);
                    candidate(data, ++count, "逃跑", 5, 30);
                }
            }
            if (!StatusEffects.has(entity, "effect_033") && distance < 3 && !accelerated) {
                candidate(data, ++count, "普攻", 10, 10);
            }
        }
        int total = 0;
        for (int i = 1; i <= count; i++) total += (int) data.candidateWeight((int) (i));
        int roll = random(1, total);
        int cumulative = 0;
        for (int i = 1; i <= count; i++) {
            cumulative += (int) data.candidateWeight((int) (i));
            if (cumulative >= roll) {
                data.setQueuedSkill(data.candidateSkill((int) (i)));
                data.setQueuedTimer(data.candidateTimer((int) (i)));
                break;
            }
        }
        data.setCandidateCount(count);
    }

    private static void duplicate(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("准备");
        } else if (phase.equals("准备") && data.tick() >= 40) {
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("smokedeeporange"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(), 40, entity.getBbHeight(), entity.getBbHeight(), 0, 1);
                for (int i = 0; i < 2; i++) {
                    YaoshouEntity copy = (YaoshouEntity) entity.getType().create(level);
                    copy.moveTo(entity.getX(), entity.getY(), entity.getZ(), level.getRandom().nextFloat() * 360F, 0);
                    Relations.shareEntityAllegiance(entity, copy);
                    EntityAttributes.setLevel(copy, Math.ceil(EntityAttributes.level(entity) / 2));
                    CreatureData.state(copy).setCountdown(200);
                    Scoreboard scoreboard = level.getScoreboard();
                    Objective objective = scoreboard.getObjective("zhaohuanwu");
                    if (objective == null) {
                        objective = scoreboard.addObjective("zhaohuanwu", ObjectiveCriteria.DUMMY,
                                Component.literal("zhaohuanwu"), ObjectiveCriteria.RenderType.INTEGER, false, null);
                    }
                    scoreboard.getOrCreatePlayerScore(copy, objective).set(1);
                    CreatureData.state(copy).setAttackerUuid(data.attackerUuid());
                    CreatureData.state(copy).setSkill("逃跑");
                    copy.finalizeSpawn(level, level.getCurrentDifficultyAt(copy.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                    level.addFreshEntity(copy);
                }
            }
            data.setPhase("结束");
        } else if (phase.equals("准备")) {
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("starorange"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(), 3, entity.getBbHeight(), entity.getBbHeight(), 0, 0);
                level.sendParticles(Particles.type("brokendeeporange"), entity.getX(), entity.getY(), entity.getZ(),
                        1, entity.getBbWidth(), 0, entity.getBbWidth(), 0);
            }
        } else if (phase.equals("结束") && data.tick() >= 41) {
            YaoshouShared.finishSkill(entity);
            data.setSkill("逃跑");
        }
    }

    private static void candidate(CreatureState data, int index, String skill, int delay, int weight) {
        data.setCandidateSkill((int) (index), skill);
        data.setCandidateTimer((int) (index), delay);
        data.setCandidateWeight((int) (index), weight);
    }

    private static int random(int min, int max) {
        return min >= max ? min : new Random().nextInt(max - min + 1) + min;
    }

    private static void look(YaoshouEntity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES,
                new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }
}
