package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import java.util.Random;
import java.util.UUID;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 030号妖兽的来源技能选择、乱舞与金旋风过程。 */
public final class Yaoshou030Behavior {
    private Yaoshou030Behavior() { }

    public static void hurt(YaoshouEntity entity, DamageSource source) {
        Entity attacker = source.getEntity();
        if (entity.isAlive() && attacker != null && !YaoshouShared.isFriendly(entity, attacker)) {
            CompoundTag data = entity.getPersistentData();
            data.putString("linshigongjizhe", attacker.getStringUUID());
            if (data.getBoolean("beidaduan")) {
                entity.setAnimation("empty");
                data.putBoolean("beidaduan", false);
            }
            if (data.getString("jineng").isEmpty() && !entity.level().isClientSide()) entity.setAnimation("animation.yaoshou030.attacked");
        }
    }

    public static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) {
            if (entity.isNoGravity()) entity.setNoGravity(false);
            entity.setDeltaMovement(0, -0.5, 0);
            return;
        }
        if (StatusEffects.has(entity, "effect_035")) {
            if (!StatusEffects.has(entity, "skilleffect_08") && entity.isNoGravity()) {
                entity.setNoGravity(false);
                if (entity.level() instanceof ServerLevel level) level.sendParticles(Particles.type("brokenwhite"),
                        entity.getX(), entity.getY(), entity.getZ(), 20, entity.getBbWidth(), 0, entity.getBbWidth(), 0.2);
            }
        } else if (!entity.isNoGravity()) {
            entity.setNoGravity(true);
        }
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
        } else if (!target.isAlive()) {
            data.putString("gongjizhe", "");
            data.putString("linshigongjizhe", "");
            YaoshouShared.finishSkill(entity);
        } else if (data.getString("jineng").isEmpty()) {
            if (data.getString("yvdingjineng").isEmpty()) schedule(entity, target);
            else if (data.getDouble("entitytick") >= data.getDouble("yvdingjinengjishi")) activate(entity);
        } else {
            switch (data.getString("jineng")) {
                case "靠近" -> approach(entity, target);
                case "挣脱" -> YaoshouShared.escape(entity);
                case "普攻" -> attack(entity, target);
                case "乱舞" -> 乱舞(entity, target);
                case "金旋风" -> 金旋风(entity);
                default -> { }
            }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        double distance = Math.hypot(entity.getX() - target.getX(), entity.getZ() - target.getZ());
        double sourceDistance = 0;
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) {
            candidate(data, ++count, "挣脱", 10, 100);
        } else {
            if (!StatusEffects.has(entity, "effect_002") && !StatusEffects.has(entity, "effect_033")) {
            candidate(data, ++count, "靠近", 10, (int) Math.round(distance));
            // 来源中的 jvli2 在候选构建前未赋值，故这两个条件始终成立。
            if (sourceDistance < 16) {
                candidate(data, ++count, "乱舞", 5, 60);
                candidate(data, ++count, "金旋风", 10, 40);
            }
            candidate(data, ++count, "普攻", 5, 20);
            }
            if (!StatusEffects.has(entity, "effect_033")) candidate(data, ++count, "普攻", 10, 30);
        }
        int total = 0;
        for (int i = 1; i <= count; i++) total += (int) data.getDouble("jinengqvanzhong" + i);
        int roll = random(1, total);
        int cumulative = 0;
        for (int i = 1; i <= count; i++) {
            cumulative += (int) data.getDouble("jinengqvanzhong" + i);
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
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putDouble("mudidiX", target.getX() + random(-6, 6));
            data.putDouble("mudidiY", target.getY() + target.getBbHeight() / 2 + (target.onGround() ? 6 : 0));
            data.putDouble("mudidiZ", target.getZ() + random(-6, 6));
            look(entity, new Vec3(data.getDouble("mudidiX"), data.getDouble("mudidiY"), data.getDouble("mudidiZ")));
            data.putString("jinengjinxing", "移动中");
        } else if (phase.equals("移动中") && data.getDouble("entitytick") >= 40) {
            YaoshouShared.finishSkill(entity);
        } else if (phase.equals("移动中")) {
            flyTo(entity, data.getDouble("mudidiX"), data.getDouble("mudidiY"), data.getDouble("mudidiZ"));
            if (entity.distanceToSqr(data.getDouble("mudidiX"), data.getDouble("mudidiY"), data.getDouble("mudidiZ")) <= 4) {
                YaoshouShared.finishSkill(entity);
            }
        }
    }

    private static void candidate(CompoundTag data, int index, String skill, int delay, int weight) {
        data.putString("linshijineng" + index, skill);
        data.putDouble("jinengjishi" + index, delay);
        data.putDouble("jinengqvanzhong" + index, weight);
    }

    private static void activate(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putString("jineng", data.getString("yvdingjineng"));
        data.putString("yvdingjineng", "");
        data.putDouble("yvdingjinengjishi", 0);
        data.putDouble("entitytick", 0);
        for (int i = 1; i <= data.getDouble("jinengshuliang"); i++) {
            data.putString("linshijineng" + i, "");
            data.putDouble("jinengjishi" + i, 0);
            data.putDouble("jinengqvanzhong" + i, 0);
        }
        data.putDouble("jinengshuliang", 0);
    }

    private static void attack(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putString("jinengjinxing", "准备");
            // 来源错误地检查047号实体；030号普攻不发送该过程动画。
            look(entity, target);
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 3) {
            look(entity, target);
            Vec3 hit = clip(entity, 1.5 + entity.getBbWidth() / 2);
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(hit, hit).inflate(1.5)).stream()
                    .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(hit))).toList()) {
                if (!YaoshouShared.isFriendly(entity, victim)) {
                    if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT, victim.getX(),
                            victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 5, 0, 0, 0, 0.5);
                    double damage = data.getDouble("attack_Num");
                    if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                    if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                    victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage <= 0 ? 0.1 : damage));
                }
            }
            data.putString("jinengjinxing", "攻击");
        } else if (phase.equals("攻击") && data.getDouble("entitytick") >= 12) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void 乱舞(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putDouble("mubiaoX", target.getX() + random(-8, 8));
            data.putDouble("mubiaoY", target.getY() + random(0, 8));
            data.putDouble("mubiaoZ", target.getZ() + random(-8, 8));
            data.putString("jinengjinxing", "走位");
        } else if (phase.equals("走位") && data.getDouble("entitytick") >= 20) {
            data.putString("jinengjinxing", "飞扑");
        } else if (phase.equals("走位") || phase.equals("离开")) {
            flyTo(entity, data.getDouble("mubiaoX"), data.getDouble("mubiaoY"), data.getDouble("mubiaoZ"));
            if (entity.distanceToSqr(data.getDouble("mubiaoX"), data.getDouble("mubiaoY"), data.getDouble("mubiaoZ")) <= 4) {
                if (phase.equals("走位")) data.putString("jinengjinxing", "飞扑");
                else data.putString("jinengjinxing", "结束");
            }
            if (phase.equals("离开")) data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (phase.equals("离开") && data.getDouble("entitytick2") >= 20) {
                data.putString("jinengjinxing", "结束");
                data.putDouble("entitytick2", 0);
            }
        } else if (phase.equals("飞扑")) {
            look(entity, target);
            entity.setDeltaMovement(entity.getLookAngle());
            if (entity.distanceTo(target) <= 2) {
                Vec3 hit = clip(entity, 1.5 + entity.getBbWidth() / 2);
                for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(hit, hit).inflate(1.5)).stream()
                        .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(hit))).toList()) {
                    if (!YaoshouShared.isFriendly(entity, victim) && entity.level() instanceof ServerLevel level) {
                        level.sendParticles(Particles.type("smallexplosiongolden"), victim.getX(), victim.getY() + victim.getBbHeight() / 2,
                                victim.getZ(), 1, 0, 0, 0, 0);
                        if (victim instanceof LivingEntity living) SpellDamage.command(level, living,
                                "shanghai 金 " + entity.getStringUUID() + " false 2 1");
                    }
                }
                data.putDouble("mubiaoX", target.getX() + random(-8, 8));
                data.putDouble("mubiaoY", target.getY() + random(0, 8));
                data.putDouble("mubiaoZ", target.getZ() + random(-8, 8));
                data.putString("jinengjinxing", "离开");
            }
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (data.getDouble("entitytick2") >= 40) {
                data.putString("jinengjinxing", "离开");
                data.putDouble("entitytick2", 0);
            }
        } else if (phase.equals("结束")) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void 金旋风(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            Entity target = target(entity);
            data.putDouble("mubiaoX", target.getX() + random(-8, 8));
            data.putDouble("mubiaoY", target.getY() + random(0, 8));
            data.putDouble("mubiaoZ", target.getZ() + random(-8, 8));
            data.putString("jinengjinxing", "走位");
        } else if (phase.equals("走位") && data.getDouble("entitytick") >= 10) {
            data.putString("jinengjinxing", "瞄准");
        } else if (phase.equals("走位")) {
            flyTo(entity, data.getDouble("mubiaoX"), data.getDouble("mubiaoY"), data.getDouble("mubiaoZ"));
            entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5));
            if (entity.distanceToSqr(data.getDouble("mubiaoX"), data.getDouble("mubiaoY"), data.getDouble("mubiaoZ")) <= 4) data.putString("jinengjinxing", "瞄准");
        } else if (phase.equals("瞄准")) {
            Entity target = target(entity);
            look(entity, target);
            data.putDouble("mubiaoX", target.getX());
            data.putDouble("mubiaoY", target.getY() + target.getBbHeight() / 2);
            data.putDouble("mubiaoZ", target.getZ());
            if (!entity.level().isClientSide()) entity.setAnimation("animation.yaoshou030.skill01");
            data.putString("jinengjinxing", "冲刺");
        } else if (phase.equals("冲刺")) {
            Vec3 aim = new Vec3(data.getDouble("mubiaoX"), data.getDouble("mubiaoY"), data.getDouble("mubiaoZ"));
            look(entity, aim);
            entity.setDeltaMovement(entity.getLookAngle());
            if (entity.level() instanceof ServerLevel level) level.sendParticles(Particles.type("disappeargolden"), entity.getX(),
                    entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 5, 1, 1, 1, 0);
            Vec3 hit = clip(entity, 1);
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(hit, hit).inflate((entity.getBbWidth() + 2) / 2)).stream()
                    .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(hit))).toList()) {
                if (!YaoshouShared.isFriendly(entity, victim) && entity.level() instanceof ServerLevel level) {
                    level.sendParticles(Particles.type("smallexplosiongolden"), victim.getX(), victim.getY() + victim.getBbHeight() / 2,
                            victim.getZ(), 1, 0, 0, 0, 0);
                    if (victim instanceof LivingEntity living) SpellDamage.command(level, living,
                            "shanghai 金 " + entity.getStringUUID() + " false 8 0.5");
                }
            }
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (data.getDouble("entitytick2") >= 12) {
                data.putString("jinengjinxing", "结束");
                data.putDouble("entitytick2", 0);
            }
        } else if (phase.equals("结束")) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static Vec3 clip(YaoshouEntity entity, double distance) {
        Vec3 eye = entity.getEyePosition(1);
        BlockPos hit = entity.level().clip(new ClipContext(eye, eye.add(entity.getViewVector(1).scale(distance)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
        return new Vec3(hit.getX(), hit.getY(), hit.getZ());
    }

    private static void flyTo(YaoshouEntity entity, double x, double y, double z) {
        Vec3 position = new Vec3(x, y, z);
        look(entity, position);
        entity.setDeltaMovement(entity.getLookAngle());
    }

    private static Entity target(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try { return level.getEntity(UUID.fromString(entity.getPersistentData().getString("gongjizhe"))); }
        catch (IllegalArgumentException exception) { return null; }
    }

    private static int random(int min, int max) { return min >= max ? min : new Random().nextInt(max - min + 1) + min; }

    private static void look(YaoshouEntity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }

    private static void look(YaoshouEntity entity, Vec3 position) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, position);
    }
}
