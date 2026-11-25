package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.Random;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

/** 023号妖兽来源的加权技能选择、攻击者追踪、攻击、飞离、回旋弹与彩羽轰炸阶段。 */
public final class Yaoshou023Behavior {
    private Yaoshou023Behavior() { }

    public static void hurt(YaoshouEntity entity, Entity attacker) {
        if (!entity.isAlive() || attacker == null || YaoshouShared.isFriendly(entity, attacker)) return;
        CreatureState data = CreatureData.state(entity);
        data.setTempAttackerUuid(attacker.getStringUUID());
        if (data.interrupted()) {
            entity.setAnimation("empty");
            data.setInterrupted(false);
        }
        if (data.skill().isEmpty() && !entity.level().isClientSide()) {
            entity.setAnimation("animation.yaoshou023.attacked");
        }
    }

    public static void tick(YaoshouEntity entity) {
        CreatureState data = CreatureData.state(entity);
        if (!entity.isAlive()) {
            if (entity.isNoGravity()) entity.setNoGravity(false);
            entity.setDeltaMovement(0, -0.5, 0);
            return;
        }
        updateGravity(entity);
        data.setTick(data.tick() + 1);
        if (data.skill().isEmpty()
                && !data.attackerUuid().equals(data.tempAttackerUuid())) {
            data.setAttackerUuid(data.tempAttackerUuid());
        }
        Entity target = findTarget(entity);
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
            else activateScheduled(entity);
        } else {
            switch (data.skill()) {
                case "靠近" -> approach(entity, target);
                case "挣脱" -> YaoshouShared.escape(entity);
                case "普攻" -> attack(entity, target);
                case "飞离" -> flyAway(entity, target);
                case "回旋光弹" -> rotatingLight(entity, target);
                case "彩羽轰炸" -> featherBombardment(entity, target);
                default -> { }
            }
        }
    }

    private static void updateGravity(YaoshouEntity entity) {
        boolean disabled = entity.hasEffect(StatusEffects.holder("effect_035"));
        boolean protectedBySkill = entity.hasEffect(StatusEffects.holder("skilleffect_08"));
        if (disabled) {
            if (!protectedBySkill && entity.isNoGravity()) {
                entity.setNoGravity(false);
                if (entity.level() instanceof ServerLevel level) {
                    level.sendParticles(Particles.type("brokenwhite"), entity.getX(), entity.getY(), entity.getZ(),
                            20, entity.getBbWidth(), 0, entity.getBbWidth(), 0.2);
                }
            }
        } else if (!entity.isNoGravity()) {
            entity.setNoGravity(true);
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        double distance = entity.distanceTo(target) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) {
            candidate(data, ++count, "挣脱", 10, 100);
        } else {
            if (!entity.hasEffect(StatusEffects.holder("effect_002"))
                    && !entity.hasEffect(StatusEffects.holder("effect_033"))) {
                if (distance < 18) {
                    candidate(data, ++count, "回旋光弹", 5, 20);
                    candidate(data, ++count, "彩羽轰炸", 10, 40);
                }
                if (distance > 6) {
                    candidate(data, ++count, "靠近", 10, (int) Math.round(10 + distance));
                } else {
                    candidate(data, ++count, "普攻", 10, 15);
                    candidate(data, ++count, "飞离", 10, 40);
                }
            }
            if (!entity.hasEffect(StatusEffects.holder("effect_033")) && distance < 3) {
                candidate(data, ++count, "普攻", 10, 30);
            }
        }
        int total = 0;
        for (int index = 1; index <= count; index++) total += (int) data.candidateWeight((int) (index));
        int roll = random(1, total);
        int cumulative = 0;
        for (int index = 1; index <= count; index++) {
            cumulative += (int) data.candidateWeight((int) (index));
            if (cumulative >= roll) {
                data.setQueuedSkill(data.candidateSkill((int) (index)));
                data.setQueuedTimer(data.candidateTimer((int) (index)));
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

    private static void activateScheduled(YaoshouEntity entity) {
        CreatureState data = CreatureData.state(entity);
        if (data.tick() < data.queuedTimer()) return;
        data.setSkill(data.queuedSkill());
        data.setQueuedSkill("");
        data.setQueuedTimer(0);
        data.setTick(0);
        for (int index = 1; index <= data.candidateCount(); index++) {
            data.setCandidateSkill((int) (index), "");
            data.setCandidateTimer((int) (index), 0);
            data.setCandidateWeight((int) (index), 0);
        }
        data.setCandidateCount(0);
    }

    private static void approach(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        if (data.phase().isEmpty()) {
            data.setDestX(target.getX() + random(-7, 7));
            data.setDestY(target.getY() + target.getBbHeight() / 2 + (target.onGround() ? 8 : 0));
            data.setDestZ(target.getZ() + random(-7, 7));
            look(entity, new Vec3(data.destX(), data.destY(), data.destZ()));
            data.setPhase("移动中");
        } else if (data.phase().equals("移动中") && data.tick() >= 40) {
            YaoshouShared.finishSkill(entity);
        } else if (data.phase().equals("移动中")) {
            Vec3 destination = new Vec3(data.destX(), data.destY(), data.destZ());
            look(entity, destination);
            entity.setDeltaMovement(entity.getLookAngle());
            if (entity.position().distanceTo(destination) <= 2) YaoshouShared.finishSkill(entity);
        }
    }

    private static void attack(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            data.setPhase("准备");
            if (!entity.level().isClientSide()) entity.setAnimation("animation.yaoshou023.attack");
            look(entity, target);
        } else if (phase.equals("准备") && data.tick() >= 1) {
            look(entity, target);
            Vec3 eye = entity.getEyePosition(1);
            BlockPos hit = entity.level().clip(new ClipContext(eye,
                    eye.add(entity.getViewVector(1).scale(0.5 + entity.getBbWidth() / 2)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
            Vec3 center = new Vec3(hit.getX(), hit.getY(), hit.getZ());
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class,
                    new AABB(center, center).inflate(1), candidate -> true).stream()
                    .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (!YaoshouShared.isFriendly(entity, victim)) {
                    if (entity.level() instanceof ServerLevel level) {
                        level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2,
                                victim.getZ(), 5, 0, 0, 0, 0.5);
                    }
                    double damage = EntityAttributes.stat(entity, Stat.ATTACK);
                    if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                    if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                    if (damage <= 0) damage = 0.1;
                    victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) damage);
                }
            }
            data.setPhase("攻击");
        } else if (phase.equals("攻击") && data.tick() >= 5) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void flyAway(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("准备");
        } else if (phase.equals("准备") && data.tick() >= 2) {
            entity.setYRot(entity.getYRot() + 180);
            entity.setXRot(0);
            entity.setYBodyRot(entity.getYRot());
            entity.setYHeadRot(entity.getYRot());
            entity.yRotO = entity.getYRot();
            entity.xRotO = entity.getXRot();
            entity.yBodyRotO = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            data.setPhase("结束");
        } else if (phase.equals("结束") && data.tick() >= 30) {
            YaoshouShared.finishSkill(entity);
        } else if (phase.equals("结束")) {
            entity.setDeltaMovement(entity.getLookAngle().add(0, 0.1, 0));
        }
    }

    private static void rotatingLight(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setRandomCap(random(120, 360));
            data.setPhase("盘旋");
        } else if (phase.equals("结束")) {
            YaoshouShared.finishSkill(entity);
        } else if (phase.equals("盘旋")) {
            double angle = Math.toRadians(data.tick() * 5);
            double targetY = target.getY() + (target.onGround() ? 10 : 3);
            Vec3 destination = new Vec3(target.getX() + 10 * Math.cos(angle), targetY, target.getZ() + 10 * Math.sin(angle));
            look(entity, destination);
            entity.setDeltaMovement(entity.getLookAngle().scale(0.5));
            if (data.tick() >= data.randomCap()) data.setPhase("结束");
            data.setTick2(data.tick2() + 1);
            if (data.tick2() >= 20) {
                if (entity.level() instanceof ServerLevel) {
                Vec3 direction = new Vec3((target.getX() - entity.getX()) * 0.5, (target.getY() - entity.getY()) * 0.5,
                        (target.getZ() - entity.getZ()) * 0.5);
                SkillSummons.shootLight01(entity, entity.position().add(0, entity.getBbHeight() / 2, 0), direction);
                }
                data.setTick2(0);
            }
        }
    }

    private static void featherBombardment(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("开始轰炸");
        } else if (phase.equals("开始轰炸") && data.tick() >= 40) {
            data.setPhase("结束");
        } else if (phase.equals("结束") && data.tick() >= 44) {
            YaoshouShared.finishSkill(entity);
        } else if (phase.equals("开始轰炸")) {
            entity.setDeltaMovement(entity.getLookAngle().x * 0.3, 0.02, entity.getLookAngle().z * 0.3);
            data.setTick2(data.tick2() + 1);
            if (data.tick2() >= 3) {
                if (entity.level() instanceof ServerLevel level) {
                Yaoshou023BaodanEntity arrow = new Yaoshou023BaodanEntity(
                        CreatureSetup.<Yaoshou023BaodanEntity>entityType("projectile_yaoshou_023baodan").get(), level);
                arrow.setOwner(entity);
                arrow.configure(0.1, 1);
                arrow.setPos(entity.getX() + new Random().nextDouble() * 2 - 1, entity.getY(), entity.getZ() + new Random().nextDouble() * 2 - 1);
                arrow.shoot(0, -1, 0, 0.5F, 10);
                level.addFreshEntity(arrow);
                }
                data.setTick2(0);
            }
        }
    }

    private static Entity findTarget(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try {
            return level.getEntity(UUID.fromString(CreatureData.state(entity).attackerUuid()));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void look(Entity entity, Entity target) {
        look(entity, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }

    private static void look(Entity entity, Vec3 target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, target);
    }

    private static int random(int minimum, int maximum) {
        return maximum <= minimum ? minimum : new Random().nextInt(maximum - minimum + 1) + minimum;
    }
}
