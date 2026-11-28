package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.Random;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

/** 001号妖兽的受击目标、加权选招与完整技能阶段。 */
public final class Yaoshou001Behavior {
    private Yaoshou001Behavior() { }

    public static void hurt(YaoshouEntity entity, DamageSource source) {
        hurt(entity, source, "animation.yaoshou001.attacked");
    }

    static void hurt(YaoshouEntity entity, DamageSource source, String attackedAnimation) {
        Entity attacker = source.getEntity();
        if (entity.isAlive() && attacker != null && !YaoshouShared.isFriendly(entity, attacker)) {
            CreatureState data = CreatureData.state(entity);
            data.setTempAttackerUuid(attacker.getStringUUID());
            if (data.interrupted()) {
                entity.setAnimation("empty");
                data.setInterrupted(false);
            }
            if (data.skill().isEmpty() && !entity.level().isClientSide()) {
                entity.setAnimation(attackedAnimation);
            }
        }
    }

    public static void tick(YaoshouEntity entity) {
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
        } else if (!target.isAlive()) {
            data.setAttackerUuid("");
            data.setTempAttackerUuid("");
            YaoshouShared.finishSkill(entity);
        } else if (data.skill().isEmpty()) {
            if (data.queuedSkill().isEmpty()) schedule(entity, target);
            else beginScheduled(entity);
        } else {
            switch (data.skill()) {
                case "靠近" -> approach(entity, target);
                case "挣脱" -> YaoshouShared.escape(entity);
                case "普攻" -> attack(entity, target, "animation.yaoshou001.attack");
                case "增幅法攻" -> amplify(entity);
                case "冲刺" -> sprint(entity, target);
                case "扎根" -> root(entity);
                case "花粉散播" -> pollen(entity, target, "animation.yaoshou001.skill01");
                case "鞭挞" -> whip(entity, target, "animation.yaoshou001.skill03");
                default -> { }
            }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        double distance = entity.distanceTo(target) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) candidate(data, ++count, "挣脱", 10, 100);
        else {
            if (!entity.hasEffect(StatusEffects.holder("effect_002")) && !entity.hasEffect(StatusEffects.holder("effect_033"))) {
                if (distance > 8) {
                    candidate(data, ++count, "增幅法攻", 40, 30);
                    candidate(data, ++count, "冲刺", 20, 60);
                } else if (distance >= 2) {
                    if (entity.getHealth() < entity.getMaxHealth() / 2) candidate(data, ++count, "扎根", 20, 50);
                    candidate(data, ++count, "靠近", 10, Math.round(distance * 10));
                    candidate(data, ++count, "鞭挞", 20, 70);
                } else {
                    candidate(data, ++count, "花粉散播", 40, 30);
                    candidate(data, ++count, "鞭挞", 20, 40);
                }
            }
            if (!entity.hasEffect(StatusEffects.holder("effect_033")) && distance < 2) candidate(data, ++count, "普攻", 20, 30);
        }
        choose(data, count);
    }

    static void beginScheduled(YaoshouEntity entity) {
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

    static void candidate(CreatureState data, int index, String skill, int delay, double weight) {
        data.setCandidateSkill((int) (index), skill);
        data.setCandidateTimer((int) (index), delay);
        data.setCandidateWeight((int) (index), weight);
    }

    static void choose(CreatureState data, int count) {
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

    static void approach(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        if (data.phase().isEmpty()) {
            look(entity, target);
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.setPhase("移动中");
        } else if (data.phase().equals("移动中") && data.tick() >= 40) YaoshouShared.finishSkill(entity);
    }

    static void attack(YaoshouEntity entity, Entity target, String animation) {
        CreatureState data = CreatureData.state(entity);
        if (data.phase().isEmpty()) {
            data.setPhase("准备");
            animate(entity, animation);
            look(entity, target);
        } else if (data.phase().equals("准备") && data.tick() >= 5) {
            Vec3 center = Vec3.atLowerCornerOf(trace(entity, 2 + entity.getBbWidth() / 2));
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1.5), candidate -> true)
                    .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (YaoshouShared.isFriendly(entity, victim)) continue;
                if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 5, 0, 0, 0, 0.5);
                double damage = EntityAttributes.stat(entity, Stat.ATTACK);
                if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage <= 0 ? 0.1 : damage));
            }
            data.setPhase("攻击");
        } else if (data.phase().equals("攻击") && data.tick() >= 11) YaoshouShared.finishSkill(entity);
    }

    private static void amplify(YaoshouEntity entity) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            animate(entity, "animation.yaoshou001.skill01");
            data.setPhase("吸收开始");
        } else if (phase.equals("吸收开始") && data.tick() >= 12) {
            for (int i = 0; i < 6; i++) SkillDelays.after(2 + i * 5, () -> {
                particles(entity, "striplightblue", 5, entity.getBbWidth(), 0, entity.getBbWidth(), 0, 0);
                particles(entity, "stripblue", 10, entity.getBbWidth(), 0, entity.getBbWidth(), 0, 0);
                particles(entity, "starlightblue", 8, entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth(), 0, entity.getBbHeight() / 2);
            });
            entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_013"), 400, (int) Math.floor(EntityAttributes.level(entity) / 10)));
            data.setPhase("结束");
        } else if (phase.equals("结束") && data.tick() >= 28) YaoshouShared.finishSkill(entity);
    }

    private static void sprint(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            data.setPhase("起飞");
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 1, false, false));
        } else if (phase.equals("起飞") && data.tick() >= 20) {
            animate(entity, "animation.yaoshou001.sprint");
            look(entity, target);
            entity.setDeltaMovement(entity.getLookAngle().x, 0.2, entity.getLookAngle().z);
            data.setPhase("冲刺");
        } else if (phase.equals("冲刺") && data.tick() >= 40) YaoshouShared.finishSkill(entity);
    }

    private static void root(YaoshouEntity entity) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            animate(entity, "animation.yaoshou001.skill02");
            data.setPhase("吸收开始");
        } else if (phase.equals("吸收开始") && data.tick() >= 28) {
            data.setTick2(data.tick2() + 1);
            if (data.tick2() >= 20) {
                if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 8, 0.2, 1, 0.2, 0);
                entity.setHealth((float) (entity.getHealth() + EntityAttributes.stat(entity, Stat.MANA_ATT) / 2));
                data.setTick2(0);
            }
            if (data.tick() >= 264) data.setPhase("结束");
        } else if (phase.equals("结束") && data.tick() >= 274) YaoshouShared.finishSkill(entity);
    }

    static void pollen(YaoshouEntity entity, Entity target, String animation) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("释放");
            animate(entity, animation);
        } else if (phase.equals("释放") && data.tick() >= 12) {
            particles(entity, "peachflowerlizi", 30, 4, 1, 4, 0, entity.getBbHeight());
            particles(entity, "smokelightred", 20, 4, 1, 4, 0.1, entity.getBbHeight());
            Vec3 center = entity.position();
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(5), candidate -> true)
                    .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (!YaoshouShared.isFriendly(entity, victim) && victim instanceof LivingEntity living) living.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_01"), 200, 0));
            }
            data.setPhase("结束");
        } else if (phase.equals("结束") && data.tick() >= 28) YaoshouShared.finishSkill(entity);
    }

    static void whip(YaoshouEntity entity, Entity target, String animation) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("释放");
            animate(entity, animation);
        } else if (phase.equals("释放") && data.tick() >= 20) {
            data.setPhase("结束");
            if (entity.level() instanceof ServerLevel level) {
                BlockPos position = trace(entity, 1 + entity.getBbWidth() / 2);
                SkillSummons.summonVineWhip(level, entity, Vec3.atLowerCornerOf(position));
            }
        } else if (phase.equals("结束") && data.tick() >= 40) YaoshouShared.finishSkill(entity);
    }

    static void animate(YaoshouEntity entity, String animation) {
        if (!entity.level().isClientSide()) entity.setAnimation(animation);
    }

    static Entity target(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try { return level.getEntity(UUID.fromString(CreatureData.state(entity).attackerUuid())); }
        catch (IllegalArgumentException exception) { return null; }
    }

    static BlockPos trace(YaoshouEntity entity, double distance) {
        return entity.level().clip(new ClipContext(entity.getEyePosition(1), entity.getEyePosition(1).add(entity.getViewVector(1).scale(distance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
    }

    static void particles(YaoshouEntity entity, String id, int count, double dx, double dy, double dz, double speed, double height) {
        if (entity.level() instanceof ServerLevel level) level.sendParticles(Particles.type(id), entity.getX(), entity.getY() + height, entity.getZ(), count, dx, dy, dz, speed);
    }

    static int random(int min, int max) { return min >= max ? min : new Random().nextInt(max - min + 1) + min; }
    static void look(YaoshouEntity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }
}
