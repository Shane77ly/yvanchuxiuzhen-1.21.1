package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.Random;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 019号妖兽的来源技能选择、隐身、逃跑及近身攻击流程。 */
public final class Yaoshou019Behavior {
    private Yaoshou019Behavior() {
    }

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
                entity.setAnimation("animation.yaoshou019.attacked");
            }
        }
    }

    public static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) {
            return;
        }
        CreatureState data = CreatureData.state(entity);
        data.setTick(data.tick() + 1);
        if (data.skill().isEmpty()
                && !data.attackerUuid().equals(data.tempAttackerUuid())) {
            data.setAttackerUuid(data.tempAttackerUuid());
        }
        Entity target = Yaoshou037054Support.target(entity.level(), entity);
        if (target == null) {
            if (data.tick() >= 100) {
                entity.getNavigation().moveTo(entity.getX() + random(-3, 3), entity.getY(),
                        entity.getZ() + random(-3, 3), 1);
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
                case "靠近" -> approach(entity, target);
                case "挣脱" -> YaoshouShared.escape(entity);
                case "普攻" -> attack(entity, target, "animation.yaoshou019.attack");
                case "隐身" -> invisibility(entity, target);
                case "逃跑" -> flee(entity, target, "smokedarkyellow");
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
            boolean invisible = entity.hasEffect(MobEffects.INVISIBILITY);
            if (!entity.hasEffect(StatusEffects.holder("effect_002"))
                    && !entity.hasEffect(StatusEffects.holder("effect_033"))) {
                if (distance > 16) {
                    if (!invisible) {
                        candidate(data, ++count, "隐身", 5, 100);
                    }
                } else if (distance > 3) {
                    candidate(data, ++count, "逃跑", 5, 30);
                    if (!invisible) {
                        candidate(data, ++count, "靠近", 5, 10);
                    }
                } else {
                    if (!invisible) {
                        candidate(data, ++count, "普攻", 10, 20);
                    }
                    candidate(data, ++count, "逃跑", 5, 30);
                }
            }
            if (!entity.hasEffect(StatusEffects.holder("effect_033")) && distance < 3 && !invisible) {
                candidate(data, ++count, "普攻", 10, 10);
            }
        }
        int total = 0;
        for (int i = 1; i <= count; i++) {
            total += (int) data.candidateWeight((int) (i));
        }
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

    private static void candidate(CreatureState data, int index, String skill, int delay, int weight) {
        data.setCandidateSkill((int) (index), skill);
        data.setCandidateTimer((int) (index), delay);
        data.setCandidateWeight((int) (index), weight);
    }

    public static void approach(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        if (data.phase().isEmpty()) {
            look(entity, target);
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.setPhase("移动中");
        } else if (data.phase().equals("移动中") && data.tick() >= 40) {
            YaoshouShared.finishSkill(entity);
        }
    }

    public static void attack(YaoshouEntity entity, Entity target, String animation) {
        CreatureState data = CreatureData.state(entity);
        if (data.phase().isEmpty()) {
            data.setPhase("准备");
            if (!entity.level().isClientSide()) {
                entity.setAnimation(animation);
            }
            look(entity, target);
        } else if (data.phase().equals("准备") && data.tick() >= 1) {
            Vec3 eye = entity.getEyePosition(1);
            BlockPos position = entity.level().clip(new ClipContext(eye,
                    eye.add(entity.getViewVector(1).scale(1 + entity.getBbWidth() / 2)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
            Vec3 center = new Vec3(position.getX(), position.getY(), position.getZ());
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class,
                    new AABB(center, center).inflate(1), candidate -> true).stream()
                    .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (!YaoshouShared.isFriendly(entity, victim)) {
                    if (entity.level() instanceof ServerLevel level) {
                        level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2,
                                victim.getZ(), 5, 0, 0, 0, 0.5);
                    }
                    double damage = EntityAttributes.stat(entity, Stat.ATTACK);
                    if (entity.hasEffect(MobEffects.WEAKNESS)) {
                        damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                    }
                    if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) {
                        damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                    }
                    victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity),
                            (float) (damage <= 0 ? 0.1 : damage));
                }
            }
            data.setPhase("攻击");
        } else if (data.phase().equals("攻击") && data.tick() >= 4) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void invisibility(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("准备");
        } else if (phase.equals("准备") && data.tick() >= 40) {
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("brokendarkyellow"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(), 24, 0, entity.getBbHeight(), 0, 1);
            }
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
            data.setPhase("结束");
        } else if (phase.equals("准备")) {
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("brokendarkyellow"), entity.getX(), entity.getY(), entity.getZ(),
                        1, entity.getBbWidth(), 0, entity.getBbWidth(), 0);
                for (int i = 1; i <= 24; i++) {
                    level.sendParticles(Particles.type("smokewhite"), entity.getX() + Math.cos(i * 5) * 2,
                            entity.getY(), entity.getZ() + Math.sin(i * 5) * 2, 1, 0, 0, 0, 0);
                }
            }
        } else if (phase.equals("结束") && data.tick() >= 41) {
            YaoshouShared.finishSkill(entity);
            data.setSkill("逃跑");
        }
    }

    /** 019与020共用的来源逃跑流程，粒子名称由调用者按编号传入。 */
    public static void flee(YaoshouEntity entity, Entity target, String particle) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            data.setPhase("准备");
        } else if (phase.equals("准备") && data.tick() >= 5) {
            entity.getNavigation().moveTo(entity.getX() + random(-16, 16), entity.getY(), entity.getZ() + random(-16, 16), 2);
            data.setPhase("结束");
        } else if (phase.equals("结束") && data.tick() >= 25) {
            if (entity.position().distanceTo(target.position()) >= 16) {
                data.setAttackerUuid("");
                data.setTempAttackerUuid("");
            }
            YaoshouShared.finishSkill(entity);
        } else if (phase.equals("结束") && entity.level() instanceof ServerLevel level) {
            level.sendParticles(Particles.type(particle), entity.getX(), entity.getY(), entity.getZ(),
                    1, entity.getBbWidth(), 0, entity.getBbWidth(), 0);
        }
    }

    private static int random(int min, int max) {
        return min >= max ? min : new Random().nextInt(max - min + 1) + min;
    }

    private static void look(YaoshouEntity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES,
                new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }
}
