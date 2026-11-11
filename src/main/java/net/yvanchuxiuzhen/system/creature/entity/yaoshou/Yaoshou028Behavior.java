package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 028号妖兽的来源技能选择、近身攻击、水流与喷水阶段。 */
final class Yaoshou028Behavior {
    private Yaoshou028Behavior() { }

    static void tick(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        if (!entity.isAlive()) return;
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        if (data.getString("jineng").isEmpty() && !data.getString("gongjizhe").equals(data.getString("linshigongjizhe"))) {
            data.putString("gongjizhe", data.getString("linshigongjizhe"));
        }
        Entity target = Yaoshou037054Support.target(entity.level(), entity);
        if (target == null) {
            if (data.getDouble("entitytick") >= 40) {
                entity.getNavigation().moveTo(entity.getX() + Yaoshou037054Support.randomInt(-3, 3), entity.getY(),
                        entity.getZ() + Yaoshou037054Support.randomInt(-3, 3), 1);
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
            else if (data.getDouble("entitytick") >= data.getDouble("yvdingjinengjishi")) activate(entity);
            return;
        }
        switch (data.getString("jineng")) {
            case "靠近" -> approach(entity, target);
            case "挣脱" -> YaoshouShared.escape(entity);
            case "普攻" -> attack(entity, target);
            case "喷水加速" -> sprayAccelerate(entity, target);
            case "水流" -> waterJet(entity, target);
            default -> { }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        double distance = entity.distanceTo(target) - (entity.getBbWidth() + target.getBbWidth()) / 2;
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) {
            candidate(data, ++count, "挣脱", 10, 100);
        } else {
            if (!StatusEffects.has(entity, "effect_002") && !StatusEffects.has(entity, "effect_033")) {
                if (distance > 7) {
                    candidate(data, ++count, "靠近", 5, 30);
                    candidate(data, ++count, "喷水加速", 10, 30);
                } else if (distance > 3) {
                    candidate(data, ++count, "靠近", 10, 30);
                    candidate(data, ++count, "水流", 40, 30);
                    candidate(data, ++count, "喷水加速", 10, 30);
                } else {
                    candidate(data, ++count, "普攻", 10, 20);
                    candidate(data, ++count, "水流", 40, 30);
                }
            }
            if (!StatusEffects.has(entity, "effect_033") && distance < 3) candidate(data, ++count, "普攻", 10, 20);
        }
        choose(data, count);
    }

    private static void candidate(CompoundTag data, int index, String skill, int delay, int weight) {
        data.putString("linshijineng" + index, skill);
        data.putDouble("jinengjishi" + index, delay);
        data.putDouble("jinengqvanzhong" + index, weight);
    }

    private static void choose(CompoundTag data, int count) {
        int total = 0;
        for (int i = 1; i <= count; i++) total += (int) data.getDouble("jinengqvanzhong" + i);
        int roll = Yaoshou037054Support.randomInt(1, total);
        int weight = 0;
        for (int i = 1; i <= count; i++) {
            weight += (int) data.getDouble("jinengqvanzhong" + i);
            if (weight >= roll) {
                data.putString("yvdingjineng", data.getString("linshijineng" + i));
                data.putDouble("yvdingjinengjishi", data.getDouble("jinengjishi" + i));
                break;
            }
        }
        data.putDouble("jinengshuliang", count);
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

    private static void approach(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        if (data.getString("jinengjinxing").isEmpty()) {
            look(entity, target);
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), entity.isInWaterOrBubble() ? 3 : 1);
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
            entity.setAnimation("animation.yaoshou028.attack");
            look(entity, target);
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 2) {
            Vec3 center = clip(entity, 1.5 + entity.getBbWidth() / 2);
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2))
                    .stream().sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
                if (Relations.isFriendly(entity, victim)) continue;
                if (entity.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2,
                            victim.getZ(), 5, 0, 0, 0, 0.5);
                }
                double damage = data.getDouble("attack_Num");
                if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) Math.max(0.1, damage));
            }
            data.putString("jinengjinxing", "攻击");
        } else if (phase.equals("攻击") && data.getDouble("entitytick") >= 10) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void sprayAccelerate(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 20) {
            data.putString("jinengjinxing", "喷射");
        } else if (phase.equals("喷射") && data.getDouble("entitytick") >= 40) {
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("喷射")) {
            look(entity, target);
            entity.setDeltaMovement(entity.getLookAngle());
            if (entity.level() instanceof ServerLevel server) {
                Vec3 direction = entity.getLookAngle().scale(-1);
                server.sendParticles(Particles.type("disappearblue"), entity.getX(), entity.getY(), entity.getZ(),
                        0, direction.x, direction.y, direction.z, 1);
                server.sendParticles(ParticleTypes.BUBBLE_POP, entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(), 5, 0.7, 0.7, 0.7, 0);
            }
        } else if (phase.equals("蓄力") && entity.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.BUBBLE_POP, entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                    entity.getZ(), 10, 0.7, 0.7, 0.7, 0);
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 41) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void waterJet(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            entity.setAnimation("animation.yaoshou028.skill01");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 4) {
            data.putString("jinengjinxing", "喷射");
        } else if (phase.equals("喷射") && data.getDouble("entitytick") >= 20) {
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("喷射") && entity.level() instanceof ServerLevel server) {
            Yaoshou028shuiqiangEntity.shoot(CreatureSetup.<Yaoshou028shuiqiangEntity>entityType("projectile_yaoshou_028shuiqiang").get(),
                    server, entity, entity.position().add(0, entity.getBbHeight() / 2, 0),
                    entity.getLookAngle().add(0, 0.1, 0), 1.5F, 0);
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 26) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static Vec3 clip(YaoshouEntity entity, double distance) {
        return Vec3.atLowerCornerOf(entity.level().clip(new ClipContext(entity.getEyePosition(),
                entity.getEyePosition().add(entity.getViewVector(1).scale(distance)), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, entity)).getBlockPos());
    }

    private static void look(Entity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES,
                new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }
}
