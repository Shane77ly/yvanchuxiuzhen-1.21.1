package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.Random;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

/** 031号妖兽的来源受击目标、候选权重和逐刻技能闭包。 */
final class Yaoshou031Behavior {
    private Yaoshou031Behavior() { }

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
            entity.setAnimation("animation.yaoshou031.attacked");
        }
    }

    static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) return;
        CreatureState data = CreatureData.state(entity);
        data.setTick(data.tick() + 1);
        if (data.skill().isEmpty() && !data.attackerUuid().equals(data.tempAttackerUuid())) {
            data.setAttackerUuid(data.tempAttackerUuid());
        }
        Entity target = Yaoshou037054Support.target(entity.level(), entity);
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
            case "水流" -> waterFlow(entity, target);
            case "水球术" -> waterBall(entity, target);
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
                if (distance < 8) {
                    candidate(data, ++count, "水流", 10, 30);
                    candidate(data, ++count, "水球术", 10, 30);
                }
                if (distance > 3) candidate(data, ++count, "靠近", 5, 30);
                else candidate(data, ++count, "普攻", 10, 20);
            }
            if (!StatusEffects.has(entity, "effect_033") && distance < 3) candidate(data, ++count, "普攻", 10, 30);
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
            animate(entity, "animation.yaoshou031.attack");
            look(entity, target);
        } else if (phase.equals("准备") && data.tick() >= 4) {
            Vec3 center = Vec3.atLowerCornerOf(trace(entity, 1.5 + entity.getBbWidth() / 2));
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
        } else if (phase.equals("攻击") && data.tick() >= 12) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void waterFlow(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("蓄力");
            entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 96, 1, false, false));
        } else if (phase.equals("蓄力") && data.tick() >= 100) {
            data.setPhase("结束");
        } else if (phase.equals("蓄力") && data.tick() >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY(), target.getZ()));
            data.setTick2(data.tick2() + 1);
            if (data.tick2() >= 3) {
                if (entity.level() instanceof ServerLevel level) {
                    level.playSound(null, entity.blockPosition(), sound("yvanchuxiuzhen:yaoshou048skill01"), SoundSource.HOSTILE, 1, 0.7F);
                } else {
                    entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                            sound("yvanchuxiuzhen:yaoshou048skill01"), SoundSource.HOSTILE, 1, 0.7F, false);
                }
                if (entity.level() instanceof ServerLevel level) {
                    Yaoshou031WaterProjectile projectile = Yaoshou031Projectiles.create(level, entity);
                    projectile.setPos(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ());
                    Vec3 look = entity.getLookAngle();
                    projectile.shoot(look.x, look.y + 0.15, look.z, 1, 10);
                    level.addFreshEntity(projectile);
                }
                data.setTick2(0);
            }
        } else if (phase.equals("蓄力") && entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FISHING, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 10,
                    entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth(), 0);
        } else if (phase.equals("结束") && data.tick() >= 44) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void waterBall(YaoshouEntity entity, Entity target) {
        CreatureState data = CreatureData.state(entity);
        String phase = data.phase();
        if (phase.isEmpty()) {
            look(entity, target);
            data.setPhase("蓄力");
            animate(entity, "animation.yaoshou031.attack");
        } else if (phase.equals("蓄力") && data.tick() >= 4) {
            look(entity, target);
            Vec3 direction = entity.getLookAngle();
            if (entity.level() instanceof ServerLevel) {
                SkillSummons.shootWater03(entity, entity.position().add(0, entity.getBbHeight() / 2, 0),
                        new Vec3(direction.x, direction.y + 0.1, direction.z));
            }
            data.setPhase("结束");
        } else if (phase.equals("结束") && data.tick() >= 12) {
            YaoshouShared.finishSkill(entity);
        }
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

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
    }
}
