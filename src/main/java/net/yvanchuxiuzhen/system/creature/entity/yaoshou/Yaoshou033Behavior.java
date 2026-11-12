package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import com.mojang.util.UndashedUuid;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.sounds.SoundSource;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 033号妖兽的来源受击目标、候选权重和逐刻技能闭包。 */
final class Yaoshou033Behavior {
    private Yaoshou033Behavior() { }

    static void hurt(YaoshouEntity entity, DamageSource source) {
        Entity attacker = source.getEntity();
        if (!entity.isAlive() || attacker == null || Relations.isFriendly(entity, attacker)) return;
        CompoundTag data = entity.getPersistentData();
        data.putString("linshigongjizhe", attacker.getStringUUID());
        if (data.getBoolean("beidaduan")) {
            entity.setAnimation("empty");
            data.putBoolean("beidaduan", false);
        }
        if (data.getString("jineng").isEmpty() && !entity.level().isClientSide()) {
            entity.setAnimation("animation.yaoshou033.attacked");
        }
    }

    static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) return;
        entity.removeEffect(StatusEffects.holder("duyao_17"));
        CompoundTag data = entity.getPersistentData();
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        if (data.getString("jineng").isEmpty() && !data.getString("gongjizhe").equals(data.getString("linshigongjizhe"))) {
            data.putString("gongjizhe", data.getString("linshigongjizhe"));
        }
        Entity target = target(entity);
        if (target == null) {
            if (data.getDouble("entitytick") >= 40) {
                entity.getNavigation().moveTo(entity.getX() + random(-3, 3), entity.getY(), entity.getZ() + random(-3, 3), 1);
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
            else beginScheduled(entity);
            return;
        }
        switch (data.getString("jineng")) {
            case "靠近" -> approach(entity, target);
            case "挣脱" -> YaoshouShared.escape(entity);
            case "普攻" -> attack(entity, target);
            case "逃跑" -> flee(entity, target, false);
            case "放毒逃跑" -> flee(entity, target, true);
            case "孢子毒雾" -> fog(entity, target, false);
            case "混乱毒雾" -> fog(entity, target, true);
            default -> { }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        double distance = Math.sqrt(entity.distanceToSqr(target)) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) {
            candidate(data, ++count, "挣脱", 10, 100);
        } else {
            if (!StatusEffects.has(entity, "effect_002") && !StatusEffects.has(entity, "effect_033")) {
                candidate(data, ++count, "逃跑", 10, 20);
                if (distance < 8) {
                    candidate(data, ++count, "放毒逃跑", 10, 40);
                    candidate(data, ++count, "孢子毒雾", 10, 40);
                    candidate(data, ++count, "混乱毒雾", 10, 40);
                }
            }
            if (!StatusEffects.has(entity, "effect_033") && distance < 3) candidate(data, ++count, "普攻", 10, 30);
        }
        choose(data, count);
    }


    private static void beginScheduled(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (data.getDouble("entitytick") < data.getDouble("yvdingjinengjishi")) return;
        data.putString("jineng", data.getString("yvdingjineng"));
        data.putString("yvdingjineng", "");
        data.putDouble("yvdingjinengjishi", 0);
        data.putDouble("entitytick", 0);
        for (int i = 1; i <= (int) data.getDouble("jinengshuliang"); i++) {
            data.putString("linshijineng" + i, "");
            data.putDouble("jinengjishi" + i, 0);
            data.putDouble("jinengqvanzhong" + i, 0);
        }
        data.putDouble("jinengshuliang", 0);
    }

    private static void candidate(CompoundTag data, int index, String skill, int delay, double weight) {
        data.putString("linshijineng" + index, skill);
        data.putDouble("jinengjishi" + index, delay);
        data.putDouble("jinengqvanzhong" + index, weight);
    }

    private static void choose(CompoundTag data, int count) {
        double total = 0;
        for (int i = 1; i <= count; i++) total += data.getDouble("jinengqvanzhong" + i);
        int roll = random(1, (int) total);
        double cumulative = 0;
        for (int i = 1; i <= count; i++) {
            cumulative += data.getDouble("jinengqvanzhong" + i);
            if (cumulative >= roll) {
                data.putString("yvdingjineng", data.getString("linshijineng" + i));
                data.putDouble("yvdingjinengjishi", data.getDouble("jinengjishi" + i));
                break;
            }
        }
        data.putDouble("jinengshuliang", count);
    }

    private static void approach(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        if (data.getString("jinengjinxing").isEmpty()) {
            look(entity, target);
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.putString("jinengjinxing", "移动中");
        } else if (data.getString("jinengjinxing").equals("移动中") && data.getDouble("entitytick") >= 40) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void attack(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putString("jinengjinxing", "准备");
            animate(entity, "animation.yaoshou033.attack");
            look(entity, target);
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 1) {
            Vec3 center = Vec3.atLowerCornerOf(trace(entity, 1.5 + entity.getBbWidth() / 2));
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2), candidate -> true)
                    .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (Relations.isFriendly(entity, victim)) continue;
                if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 5, 0, 0, 0, 0.5);
                double damage = data.getDouble("attack_Num");
                if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage <= 0 ? 0.1 : damage));
            }
            data.putString("jinengjinxing", "攻击");
        } else if (phase.equals("攻击") && data.getDouble("entitytick") >= 5) {
            YaoshouShared.finishSkill(entity);
        }
    }

    /** 来源逃跑过程只将随机Z用于导航；随机X仍按原顺序取样。 */
    private static void flee(YaoshouEntity entity, Entity target, boolean poison) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putString("jinengjinxing", "准备");
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 5) {
            int range = poison ? 48 : 32;
            int offsetZ = 0;
            for (int i = 0; i < 6; i++) {
                random(-range, range);
                offsetZ = random(-range, range);
                if (Math.abs(offsetZ) >= 20) break;
            }
            entity.getNavigation().moveTo(entity.getX(), entity.getY(), entity.getZ() + offsetZ, poison ? 1.3 : 2);
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= (poison ? 60 : 25)) {
            YaoshouShared.finishSkill(entity);
            if (entity.distanceToSqr(target) >= 144) {
                data.putString("gongjizhe", "");
                data.putString("linshigongjizhe", "");
            }
        } else if (phase.equals("结束") && poison) {
            animate(entity, "animation.yaoshou033.skill01");
            data.putString("jinengjinxing", "喷毒");
        } else if (phase.equals("喷毒")) {
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (data.getDouble("entitytick2") >= 10) {
                sound(entity, "yaoshou033skill02");
                ring(entity, "yaoshou_033skill_01", 20, 0.5);
                if (entity.level() instanceof ServerLevel level) {
                    AreaEffectCloud cloud = new AreaEffectCloud(level, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ());
                    cloud.setDuration(60);
                    cloud.setRadius(3);
                    cloud.setRadiusOnUse(-0.01F);
                    cloud.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_17"), 200, 0));
                    cloud.setParticle(Particles.type("yaoshou_033skill_01"));
                    level.addFreshEntity(cloud);
                }
                data.putDouble("entitytick2", 0);
                data.putString("jinengjinxing", "后摇");
            }
        } else if (phase.equals("后摇")) {
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (data.getDouble("entitytick2") >= 7) {
                data.putDouble("entitytick2", 0);
                data.putString("jinengjinxing", "结束");
            }
        }
    }

    private static void fog(YaoshouEntity entity, Entity target, boolean confusion) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            sound(entity, "yaoshou033skill0102");
            data.putString("jinengjinxing", "蓄力");
            animate(entity, "animation.yaoshou033.skill02");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 22) {
            data.putString("jinengjinxing", "释放");
        } else if (phase.equals("释放") && data.getDouble("entitytick") >= 40) {
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("释放")) {
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 2);
            ring(entity, confusion ? "smokelightred" : "yaoshou_033skill_01", 5 + data.getDouble("entitytick2"), 1);
            Vec3 center = entity.position().add(0, entity.getBbHeight() / 2, 0);
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(3.5), candidate -> true)
                    .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (!Relations.isFriendly(entity, victim) && victim instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(StatusEffects.holder(confusion ? "effect_001" : "duyao_17"), confusion ? 60 : 400, 0));
                }
            }
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 50) {
            YaoshouShared.finishSkill(entity);
        }
    }

    /** 来源角度步长直接传入三角函数，单粒子速度保留命令的双重倍率。 */
    private static void ring(YaoshouEntity entity, String particle, double count, double speed) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        double angle = 0;
        for (int i = 0; i < (int) count; i++) {
            double x = Math.cos(angle);
            double z = Math.sin(angle);
            level.sendParticles(Particles.type(particle), entity.getX() + 3 * x, entity.getY() + entity.getBbHeight() * 0.5,
                    entity.getZ() + 3 * z, 0, speed * x, 0, speed * z, speed);
            angle += 360 / count;
        }
    }

    private static void sound(YaoshouEntity entity, String id) {
        if (entity.level() instanceof ServerLevel level) {
            level.playSound(null, BlockPos.containing(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ()),
                    Yaoshou037054Support.sound("yvanchuxiuzhen:" + id), SoundSource.HOSTILE, 1, 1);
        } else {
            entity.level().playLocalSound(entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                    Yaoshou037054Support.sound("yvanchuxiuzhen:" + id), SoundSource.HOSTILE, 1, 1, false);
        }
    }

    private static Entity target(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try { return level.getEntity(UndashedUuid.fromStringLenient(entity.getPersistentData().getString("gongjizhe"))); }
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
