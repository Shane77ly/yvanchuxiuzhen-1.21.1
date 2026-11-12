package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 002号妖兽的来源技能权重、阶段时序及风飘鞭/连环鞭挞分支。 */
public final class Yaoshou002Behavior {
    private Yaoshou002Behavior() { }

    public static void hurt(YaoshouEntity entity, DamageSource source) {
        boolean interrupted = entity.getPersistentData().getBoolean("beidaduan");
        Yaoshou001Behavior.hurt(entity, source, "animation.yaoshou002.attacked");
        Entity attacker = source.getEntity();
        if (interrupted && attacker != null && entity.isAlive() && !YaoshouShared.isFriendly(entity, attacker)) entity.setShiftKeyDown(false);
    }

    public static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) return;
        CompoundTag data = entity.getPersistentData();
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        if (data.getString("jineng").isEmpty() && !data.getString("gongjizhe").equals(data.getString("linshigongjizhe"))) {
            data.putString("gongjizhe", data.getString("linshigongjizhe"));
        }
        Entity target = Yaoshou001Behavior.target(entity);
        if (target == null) {
            if (data.getDouble("entitytick") >= 40) {
                entity.getNavigation().moveTo(entity.getX() + Yaoshou001Behavior.random(-3, 3), entity.getY(), entity.getZ() + Yaoshou001Behavior.random(-3, 3), 1);
                data.putDouble("entitytick", 0);
            }
            return;
        }
        if (!target.isAlive()) {
            data.putString("gongjizhe", "");
            data.putString("linshigongjizhe", "");
            YaoshouShared.finishSkill(entity);
            return;
        }
        if (data.getString("jineng").isEmpty()) {
            if (data.getString("yvdingjineng").isEmpty()) schedule(entity, target);
            else Yaoshou001Behavior.beginScheduled(entity);
            return;
        }
        switch (data.getString("jineng")) {
            case "靠近" -> Yaoshou001Behavior.approach(entity, target);
            case "挣脱" -> YaoshouShared.escape(entity);
            case "普攻" -> Yaoshou001Behavior.attack(entity, target, "animation.yaoshou002.attack");
            case "冲刺" -> sprint(entity, target);
            case "风飘鞭挞" -> windWhip(entity, target);
            case "扎根" -> root(entity);
            case "增幅法攻" -> amplify(entity);
            case "鞭挞" -> Yaoshou001Behavior.whip(entity, target, "animation.yaoshou002.skill03");
            case "连环鞭挞" -> combo(entity, target);
            case "花粉散播" -> Yaoshou001Behavior.pollen(entity, target, "animation.yaoshou002.skill03");
            default -> { }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        double distance = entity.distanceTo(target) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
        Yaoshou001Behavior.look(entity, target);
        int count = 0;
        if (entity.isPassenger()) Yaoshou001Behavior.candidate(data, ++count, "挣脱", 10, 100);
        else {
            if (!entity.hasEffect(StatusEffects.holder("effect_002")) && !entity.hasEffect(StatusEffects.holder("effect_033"))) {
                if (distance > 3) {
                    Yaoshou001Behavior.candidate(data, ++count, "靠近", 5, 20);
                    Yaoshou001Behavior.candidate(data, ++count, "冲刺", 20, 30);
                    Yaoshou001Behavior.candidate(data, ++count, "风飘鞭挞", 10, Math.round(distance * 15));
                    if (entity.getHealth() < entity.getMaxHealth() / 2) Yaoshou001Behavior.candidate(data, ++count, "扎根", 20, 20);
                    if (!entity.hasEffect(StatusEffects.holder("effect_013"))) Yaoshou001Behavior.candidate(data, ++count, "增幅法攻", 40, 30);
                }
                if (distance < 3) {
                    Yaoshou001Behavior.candidate(data, ++count, "鞭挞", 20, 30);
                    Yaoshou001Behavior.candidate(data, ++count, "连环鞭挞", 20, 30);
                    Yaoshou001Behavior.candidate(data, ++count, "普攻", 20, 10);
                }
                if (distance < 6) Yaoshou001Behavior.candidate(data, ++count, "花粉散播", 40, 30);
            }
            if (distance < 3 && !entity.hasEffect(StatusEffects.holder("effect_033"))) Yaoshou001Behavior.candidate(data, ++count, "普攻", 20, 30);
        }
        Yaoshou001Behavior.choose(data, count);
    }

    private static void root(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            Yaoshou001Behavior.animate(entity, "animation.yaoshou002.skill05");
            data.putString("jinengjinxing", "吸收开始");
            entity.setShiftKeyDown(true);
        } else if (phase.equals("吸收开始") && data.getDouble("entitytick") >= 28) {
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (data.getDouble("entitytick2") >= 20) {
                if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 8, 0.2, 1, 0.2, 0);
                entity.setHealth((float) (entity.getHealth() + data.getDouble("mana_Att") / 2));
                data.putDouble("entitytick2", 0);
            }
            if (data.getDouble("entitytick") >= 264) data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 274) {
            entity.setShiftKeyDown(false);
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void amplify(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            Yaoshou001Behavior.animate(entity, "animation.yaoshou002.ride");
            data.putString("jinengjinxing", "吸收开始");
        } else if (phase.equals("吸收开始") && data.getDouble("entitytick") >= 20) {
            for (int i = 0; i < 6; i++) SkillDelays.after(2 + i * 5, () -> {
                Yaoshou001Behavior.particles(entity, "striplightblue", 5, entity.getBbWidth(), 0, entity.getBbWidth(), 0, 0);
                Yaoshou001Behavior.particles(entity, "stripblue", 10, entity.getBbWidth(), 0, entity.getBbWidth(), 0, 0);
                Yaoshou001Behavior.particles(entity, "starlightblue", 8, entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth(), 0, entity.getBbHeight() / 2);
            });
            entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_013"), 400, (int) Math.floor(data.getDouble("LEVELnumber") / 10)));
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 40) YaoshouShared.finishSkill(entity);
    }

    private static void sprint(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putString("jinengjinxing", "起飞");
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 1, false, false));
        } else if (phase.equals("起飞") && data.getDouble("entitytick") >= 20) {
            Yaoshou001Behavior.animate(entity, "animation.yaoshou002.sprint");
            Yaoshou001Behavior.look(entity, target);
            entity.setDeltaMovement(entity.getLookAngle().x, 0.2, entity.getLookAngle().z);
            data.putString("jinengjinxing", "冲刺");
        } else if (phase.equals("冲刺") && data.getDouble("entitytick") >= 40) YaoshouShared.finishSkill(entity);
    }

    private static void windWhip(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
            Yaoshou001Behavior.animate(entity, "animation.yaoshou002.skill04");
            data.putString("jinengjinxing", "起飞");
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 1, false, false));
        } else if (phase.equals("起飞") && data.getDouble("entitytick") < 20) {
            entity.setDeltaMovement(entity.getLookAngle().x * 0.5, 0, entity.getLookAngle().z * 0.5);
        } else if (phase.equals("起飞") && data.getDouble("entitytick") >= 20) {
            data.putString("jinengjinxing", "调整姿态");
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
        } else if (phase.equals("调整姿态") && data.getDouble("entitytick") >= 32) {
            Vec3 center = Vec3.atLowerCornerOf(Yaoshou001Behavior.trace(entity, 3 + entity.getBbWidth() / 2));
            damageArea(entity, center, 2.5, data.getDouble("attack_Num"), true);
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 44) YaoshouShared.finishSkill(entity);
    }

    private static void combo(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, target.position());
            Yaoshou001Behavior.animate(entity, "animation.yaoshou002.skill01");
            data.putString("jinengjinxing", "第一击");
        } else if (phase.equals("第一击") && data.getDouble("entitytick") >= 6) {
            hitCombo(entity, data); data.putString("jinengjinxing", "第二击");
        } else if (phase.equals("第二击") && data.getDouble("entitytick") >= 12) {
            hitCombo(entity, data); data.putString("jinengjinxing", "第三击");
        } else if (phase.equals("第三击") && data.getDouble("entitytick") >= 19) {
            hitCombo(entity, data); data.putString("jinengjinxing", "第四击");
        } else if (phase.equals("第四击") && data.getDouble("entitytick") >= 26) {
            hitCombo(entity, data); data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 32) YaoshouShared.finishSkill(entity);
    }

    private static void hitCombo(YaoshouEntity entity, CompoundTag data) {
        Vec3 center = Vec3.atLowerCornerOf(Yaoshou001Behavior.trace(entity, 2 + entity.getBbWidth() / 2));
        damageArea(entity, center, 1.5, data.getDouble("LEVELnumber"), false);
    }

    private static void damageArea(YaoshouEntity entity, Vec3 center, double radius, double damage, boolean modifyDamage) {
        for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(radius), candidate -> true)
                .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
            if (YaoshouShared.isFriendly(entity, victim)) continue;
            if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + entity.getBbHeight() / 2, victim.getZ(), 5, 0, 0, 0, 0.5);
            double amount = damage;
            if (modifyDamage && entity.hasEffect(MobEffects.WEAKNESS)) amount -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
            if (modifyDamage && entity.hasEffect(MobEffects.DAMAGE_BOOST)) amount += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
            victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (modifyDamage && amount <= 0 ? 0.1 : amount));
        }
    }
}
