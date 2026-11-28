package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import com.mojang.util.UndashedUuid;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

/** 032号妖兽的来源受击目标、候选权重和逐刻技能闭包。 */
final class Yaoshou032Behavior {
    private Yaoshou032Behavior() { }

    static void hurt(YaoshouEntity entity, DamageSource source) {
        Entity attacker = source.getEntity();
        if (!entity.isAlive() || attacker == null || Relations.isFriendly(entity, attacker)) return;
        CreatureState data = CreatureData.state(entity);
        data.setTempAttackerUuid(attacker.getStringUUID());
        if (data.interrupted()) {
            entity.setAnimation("empty");
            data.setInterrupted(false);
        }
        if (data.skill().isEmpty() && !entity.level().isClientSide()) {
            entity.setAnimation("animation.yaoshou032.attacked");
        }
    }

    static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) return;
        CreatureState data = CreatureData.state(entity);
        data.setTick(data.tick() + 1);
        if (data.skill().isEmpty() && !data.attackerUuid().equals(data.tempAttackerUuid())) {
            data.setAttackerUuid(data.tempAttackerUuid());
        }
        Entity target = target(entity);
        if (target == null) {
            if (data.tick() >= 40) {
                entity.getNavigation().moveTo(entity.getX() + random(-3, 3), entity.getY(), entity.getZ() + random(-3, 3), 1);
                data.setTick(0);
            }
            return;
        }
        if (!target.isAlive()) {
            data.setAttackerUuid("");
            data.setTempAttackerUuid("");
            YaoshouShared.finishSkill(entity);
            return;
        }
        if (data.skill().isEmpty()) {
            if (data.queuedSkill().isEmpty()) schedule(entity, target);
            else beginScheduled(entity);
            return;
        }
        switch (data.skill()) {
            case "靠近" -> approach(entity, target);
            case "挣脱" -> YaoshouShared.escape(entity);
            case "普攻" -> attack(entity, target);
            case "野蛮冲撞" -> Yaoshou004Behavior.Yaoshou004yemanchongzhuang(entity.level(), entity);
            case "吃草" -> Yaoshou004Behavior.Yaoshou004chicao(entity.level(), entity);
            case "金刚顶" -> Yaoshou004Behavior.Yaoshou004jingangding(entity.level(), entity);
            case "践踏" -> Yaoshou004Behavior.Yaoshou004jianta(entity.level(), entity);
            case "乱摆头" -> Yaoshou004Behavior.Yaoshou004luanbaitou(entity.level(), entity);
            default -> { }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        double distance = Math.sqrt(entity.distanceToSqr(target)) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) {
            candidate(data, ++count, "挣脱", 10, 100);
        } else {
            if (!StatusEffects.has(entity, "effect_002") && !StatusEffects.has(entity, "effect_033")) {
                if (distance > 6) {
                    candidate(data, ++count, "野蛮冲撞", 20, 50);
                    candidate(data, ++count, "靠近", 10, 30);
                    if (entity.getHealth() < entity.getMaxHealth() / 2) candidate(data, ++count, "吃草", 20, 20);
                } else {
                    candidate(data, ++count, "金刚顶", 10, 50);
                    candidate(data, ++count, "践踏", 10, 50);
                    candidate(data, ++count, "乱摆头", 10, 50);
                    candidate(data, ++count, "普攻", 10, 20);
                    candidate(data, ++count, "靠近", 10, 50);
                }
            }
            if (distance < 3 && !StatusEffects.has(entity, "effect_033")) candidate(data, ++count, "普攻", 10, 30);
        }
        choose(data, count);
    }


    private static void beginScheduled(YaoshouEntity entity) {
        CreatureState data = CreatureData.state(entity);
        if (data.tick() < data.queuedTimer()) return;
        data.setSkill(data.queuedSkill());
        data.setQueuedSkill("");
        data.setQueuedTimer(0);
        data.setTick(0);
        for (int i = 1; i <= (int) data.candidateCount(); i++) {
            data.setCandidateSkill((int) (i), "");
            data.setCandidateTimer((int) (i), 0);
            data.setCandidateWeight((int) (i), 0);
        }
        data.setCandidateCount(0);
    }

    private static void candidate(CreatureState data, int index, String skill, int delay, double weight) {
        data.setCandidateSkill((int) (index), skill);
        data.setCandidateTimer((int) (index), delay);
        data.setCandidateWeight((int) (index), weight);
    }

    private static void choose(CreatureState data, int count) {
        double total = 0;
        for (int i = 1; i <= count; i++) total += data.candidateWeight((int) (i));
        int roll = random(1, (int) total);
        double cumulative = 0;
        for (int i = 1; i <= count; i++) {
            cumulative += data.candidateWeight((int) (i));
            if (cumulative >= roll) {
                data.setQueuedSkill(data.candidateSkill((int) (i)));
                data.setQueuedTimer(data.candidateTimer((int) (i)));
                break;
            }
        }
        data.setCandidateCount(count);
    }

    private static void approach(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        if (data.phase().isEmpty()) {
            look(entity, target);
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.setPhase("移动中");
        } else if (data.phase().equals("移动中") && data.tick() >= 40) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void attack(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            data.setPhase("准备");
            animate(entity, "animation.yaoshou032.attack");
            look(entity, target);
        } else if (phase.equals("准备") && data.tick() >= 5) {
            Vec3 center = Vec3.atLowerCornerOf(trace(entity, 3 + entity.getBbWidth() / 2));
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2), candidate -> true)
                    .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (Relations.isFriendly(entity, victim)) continue;
                if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 5, 0, 0, 0, 0.5);
                double damage = EntityAttributes.stat(entity, Stat.ATTACK);
                if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage <= 0 ? 0.1 : damage));
            }
            data.setPhase("攻击");
        } else if (phase.equals("攻击") && data.tick() >= 11) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static Entity target(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try { return level.getEntity(UndashedUuid.fromStringLenient(CreatureData.state(entity).attackerUuid())); }
        catch (IllegalArgumentException exception) { return null; }
    }

    private static BlockPos trace(YaoshouEntity entity, double distance) {
        return entity.level().clip(new ClipContext(entity.getEyePosition(1), entity.getEyePosition(1).add(entity.getViewVector(1).scale(distance)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
    }

    private static void animate(YaoshouEntity entity, String animation) {
        if (!entity.level().isClientSide()) entity.setAnimation(animation);
    }

    private static void look(YaoshouEntity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }

    private static int random(int min, int max) {
        return min >= max ? min : new Random().nextInt(max - min + 1) + min;
    }

}
