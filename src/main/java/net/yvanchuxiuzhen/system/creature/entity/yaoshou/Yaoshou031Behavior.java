package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import com.mojang.util.UndashedUuid;
import java.util.Comparator;
import java.util.Random;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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

/** 031号妖兽的来源受击目标、候选权重和逐刻技能闭包。 */
final class Yaoshou031Behavior {
    private Yaoshou031Behavior() { }

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
            entity.setAnimation("animation.yaoshou031.attacked");
        }
    }

    static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) return;
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
            case "水流" -> waterFlow(entity, target);
            case "水球术" -> waterBall(entity, target);
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
            animate(entity, "animation.yaoshou031.attack");
            look(entity, target);
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 4) {
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
        } else if (phase.equals("攻击") && data.getDouble("entitytick") >= 12) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void waterFlow(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            entity.addEffect(new MobEffectInstance(StatusEffects.holder("skilleffect_08"), 96, 1, false, false));
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 100) {
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 20) {
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY(), target.getZ()));
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (data.getDouble("entitytick2") >= 3) {
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
                data.putDouble("entitytick2", 0);
            }
        } else if (phase.equals("蓄力") && entity.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.FISHING, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 10,
                    entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth(), 0);
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 44) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void waterBall(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            animate(entity, "animation.yaoshou031.attack");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 4) {
            look(entity, target);
            Vec3 direction = entity.getLookAngle();
            if (entity.level() instanceof ServerLevel) {
                SkillSummons.shootWater03(entity, entity.position().add(0, entity.getBbHeight() / 2, 0),
                        new Vec3(direction.x, direction.y + 0.1, direction.z));
            }
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 12) {
            YaoshouShared.finishSkill(entity);
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

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id));
    }
}
