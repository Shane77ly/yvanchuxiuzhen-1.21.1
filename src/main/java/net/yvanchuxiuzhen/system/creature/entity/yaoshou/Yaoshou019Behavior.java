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
            CompoundTag data = entity.getPersistentData();
            data.putString("linshigongjizhe", attacker.getStringUUID());
            if (data.getBoolean("beidaduan")) {
                entity.setAnimation("empty");
                data.putBoolean("beidaduan", false);
            }
            if (data.getString("jineng").isEmpty() && !entity.level().isClientSide()) {
                entity.setAnimation("animation.yaoshou019.attacked");
            }
        }
    }

    public static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) {
            return;
        }
        CompoundTag data = entity.getPersistentData();
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        if (data.getString("jineng").isEmpty()
                && !data.getString("gongjizhe").equals(data.getString("linshigongjizhe"))) {
            data.putString("gongjizhe", data.getString("linshigongjizhe"));
        }
        Entity target = target(entity);
        if (target == null) {
            if (data.getDouble("entitytick") >= 100) {
                entity.getNavigation().moveTo(entity.getX() + random(-3, 3), entity.getY(),
                        entity.getZ() + random(-3, 3), 1);
                data.putDouble("entitytick", 0);
            }
        } else if (!target.isAlive()) {
            data.putString("gongjizhe", "");
            data.putString("linshigongjizhe", "");
            YaoshouShared.finishSkill(entity);
        } else if (data.getString("jineng").isEmpty()) {
            if (data.getString("yvdingjineng").isEmpty()) {
                schedule(entity, target);
            } else if (data.getDouble("entitytick") >= data.getDouble("yvdingjinengjishi")) {
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
        } else {
            switch (data.getString("jineng")) {
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
        CompoundTag data = entity.getPersistentData();
        double distance = entity.distanceTo(target) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
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
            total += (int) data.getDouble("jinengqvanzhong" + i);
        }
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

    private static void candidate(CompoundTag data, int index, String skill, int delay, int weight) {
        data.putString("linshijineng" + index, skill);
        data.putDouble("jinengjishi" + index, delay);
        data.putDouble("jinengqvanzhong" + index, weight);
    }

    public static void approach(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        if (data.getString("jinengjinxing").isEmpty()) {
            look(entity, target);
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.putString("jinengjinxing", "移动中");
        } else if (data.getString("jinengjinxing").equals("移动中") && data.getDouble("entitytick") >= 40) {
            YaoshouShared.finishSkill(entity);
        }
    }

    public static void attack(YaoshouEntity entity, Entity target, String animation) {
        CompoundTag data = entity.getPersistentData();
        if (data.getString("jinengjinxing").isEmpty()) {
            data.putString("jinengjinxing", "准备");
            if (!entity.level().isClientSide()) {
                entity.setAnimation(animation);
            }
            look(entity, target);
        } else if (data.getString("jinengjinxing").equals("准备") && data.getDouble("entitytick") >= 1) {
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
                    double damage = data.getDouble("attack_Num");
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
            data.putString("jinengjinxing", "攻击");
        } else if (data.getString("jinengjinxing").equals("攻击") && data.getDouble("entitytick") >= 4) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void invisibility(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "准备");
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 40) {
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("brokendarkyellow"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(), 24, 0, entity.getBbHeight(), 0, 1);
            }
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("准备")) {
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("brokendarkyellow"), entity.getX(), entity.getY(), entity.getZ(),
                        1, entity.getBbWidth(), 0, entity.getBbWidth(), 0);
                for (int i = 1; i <= 24; i++) {
                    level.sendParticles(Particles.type("smokewhite"), entity.getX() + Math.cos(i * 5) * 2,
                            entity.getY(), entity.getZ() + Math.sin(i * 5) * 2, 1, 0, 0, 0, 0);
                }
            }
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 41) {
            YaoshouShared.finishSkill(entity);
            data.putString("jineng", "逃跑");
        }
    }

    /** 019与020共用的来源逃跑流程，粒子名称由调用者按编号传入。 */
    public static void flee(YaoshouEntity entity, Entity target, String particle) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putString("jinengjinxing", "准备");
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 5) {
            entity.getNavigation().moveTo(entity.getX() + random(-16, 16), entity.getY(), entity.getZ() + random(-16, 16), 2);
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 25) {
            if (entity.distanceTo(target) >= 16) {
                data.putString("gongjizhe", "");
                data.putString("linshigongjizhe", "");
            }
            YaoshouShared.finishSkill(entity);
        } else if (phase.equals("结束") && entity.level() instanceof ServerLevel level) {
            level.sendParticles(Particles.type(particle), entity.getX(), entity.getY(), entity.getZ(),
                    1, entity.getBbWidth(), 0, entity.getBbWidth(), 0);
        }
    }

    private static Entity target(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return null;
        }
        String value = entity.getPersistentData().getString("gongjizhe");
        try {
            return level.getEntity(UUID.fromString(value));
        } catch (IllegalArgumentException exception) {
            return null;
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
