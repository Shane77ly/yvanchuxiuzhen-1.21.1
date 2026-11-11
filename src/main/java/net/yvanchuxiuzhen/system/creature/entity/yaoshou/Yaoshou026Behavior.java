package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Comparator;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 026号妖兽的来源候选、扑击、近战、喷火、扫尾及辅助技能阶段。 */
final class Yaoshou026Behavior {
    private Yaoshou026Behavior() { }

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
            case "飞扑" -> flyPounce(entity, target);
            case "火焰喷射" -> flameSpray(entity, target);
            case "虎啸" -> roar(entity, target);
            case "扫尾" -> tailSweep(entity, target);
            case "扑咬" -> pounceBite(entity, target);
            case "向后跳" -> hop(entity, target, 180);
            case "向左跳" -> hop(entity, target, 70);
            case "向右跳" -> hop(entity, target, -70);
            case "爆气" -> burst(entity);
            case "火焰掌" -> flamePalm(entity, target);
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
        } else if (!StatusEffects.has(entity, "effect_002") && !StatusEffects.has(entity, "effect_033")) {
            if (distance > 6) {
                candidate(data, ++count, "飞扑", 10, 50);
                candidate(data, ++count, "靠近", 5, 30);
                candidate(data, ++count, "扑咬", 20, 50);
            } else {
                candidate(data, ++count, "火焰喷射", 20, 70);
                candidate(data, ++count, "普攻", 10, 40);
                int hopWeight = StatusEffects.has(entity, "effect_034")
                        ? 10 + 10 * StatusEffects.amplifier(entity, "effect_034") : 10;
                candidate(data, ++count, "向后跳", 5, hopWeight);
                candidate(data, ++count, "向左跳", 5, hopWeight);
                candidate(data, ++count, "向右跳", 5, hopWeight);
                candidate(data, ++count, "扫尾", 10, 50);
                candidate(data, ++count, "虎啸", 20, 50);
                candidate(data, ++count, "扑咬", 10, 50);
                candidate(data, ++count, "火焰掌", 5, 50);
                if (StatusEffects.has(entity, "effect_034") && StatusEffects.amplifier(entity, "effect_034") >= 3) {
                    candidate(data, ++count, "爆气", 5, 10 + 10 * StatusEffects.amplifier(entity, "effect_034"));
                }
            }
        }
        if (!StatusEffects.has(entity, "effect_033") && distance < 3) candidate(data, ++count, "普攻", 10, 30);
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
            entity.setAnimation("animation.yaoshou026.attack");
            look(entity, target);
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 3) {
            Vec3 center = clip(entity, 1.5 + entity.getBbWidth() / 2);
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2))
                    .stream().sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
                if (Relations.isFriendly(entity, victim)) continue;
                particles(entity, victim, ParticleTypes.CRIT, 5, 0.5);
                double damage = data.getDouble("attack_Num");
                if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) Math.max(0.1, damage));
            }
            data.putString("jinengjinxing", "攻击");
        } else if (phase.equals("攻击") && data.getDouble("entitytick") >= 12) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void flyPounce(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            entity.setAnimation("animation.yaoshou026.skill06");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 13) {
            entity.setDeltaMovement(entity.getLookAngle().x * 1.5, 0.6, entity.getLookAngle().z * 1.5);
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 30) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void flameSpray(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            entity.setAnimation("animation.yaoshou026.skill05");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 20) {
            look(entity, target);
            data.putDouble("entitytick2", entity.getYRot() - 30);
            data.putString("jinengjinxing", "开始喷火");
        } else if (phase.equals("开始喷火二") && data.getDouble("entitytick") >= 60) {
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("开始喷火") && data.getDouble("entitytick") >= 40) {
            data.putString("jinengjinxing", "开始喷火二");
        } else if (phase.equals("开始喷火") || phase.equals("开始喷火二")) {
            if (entity.level() instanceof ServerLevel level) {
                double angle = Math.toRadians(data.getDouble("entitytick2"));
                Yaoshou026huoyanEntity.shoot(CreatureSetup.<Yaoshou026huoyanEntity>entityType("projectile_yaoshou_026huoyan").get(),
                        level, entity, entity.position().add(0, entity.getBbHeight() / 2, 0),
                        new Vec3(-Math.sin(angle), 0, Math.cos(angle)), 1);
            }
            data.putDouble("entitytick2", data.getDouble("entitytick2") + (phase.equals("开始喷火") ? 3 : -3));
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 70) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void tailSweep(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            entity.setAnimation("animation.yaoshou026.skill02");
            data.putString("jinengjinxing", "蓄力");
            entity.setDeltaMovement(entity.getLookAngle().x, 0.45, entity.getLookAngle().z);
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 7) {
            data.putString("jinengjinxing", "扫");
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
        } else if (phase.equals("扫") && data.getDouble("entitytick") >= 13) {
            Vec3 center = clip(entity, 3 + entity.getBbWidth() / 2);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012skill02"), SoundSource.NEUTRAL, 1.5F, 1);
            if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    center.x, center.y, center.z, 16, 5, 2, 5, 0);
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class,
                    new AABB(center, center).inflate(9, 3, 9)).stream()
                    .sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
                if (Relations.isFriendly(entity, victim)) continue;
                particles(entity, victim, Particles.type("smallexplosiondarkblue"), 1, 0);
                particles(entity, victim, Particles.type("cailiao_238particle"), 10, 0.5);
                if (entity.level() instanceof ServerLevel level && victim instanceof LivingEntity living) {
                    SpellDamage.command(level, living, "shanghai 火 " + entity.getStringUUID() + " false 3 1.5");
                }
                if (!hasSuperArmor(victim)) victim.setDeltaMovement(entity.getLookAngle().x, 0.4, entity.getLookAngle().z);
            }
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 28) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void pounceBite(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            entity.setAnimation("animation.yaoshou026.skill03");
            entity.setDeltaMovement(entity.getLookAngle().x, 0.6, entity.getLookAngle().z);
            data.putString("jinengjinxing", "起飞");
        } else if (phase.equals("起飞") && data.getDouble("entitytick") >= 17) {
            Vec3 center = clip(entity, 2 + entity.getBbWidth() / 2);
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CRIT, center.x, center.y, center.z, 20, 1, 1, 1, 1);
            }
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2.5))
                    .stream().sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
                if (Relations.isFriendly(entity, victim)) continue;
                particles(entity, victim, Particles.type("smallexplosiondarkblue"), 1, 0);
                particles(entity, victim, Particles.type("cailiao_238particle"), 10, 0.5);
                if (entity.level() instanceof ServerLevel level && victim instanceof LivingEntity living) {
                    SpellDamage.command(level, living, "shanghai 火 " + entity.getStringUUID() + " false 5 1");
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 20));
                }
            }
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 25) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void hop(YaoshouEntity entity, Entity target, double turn) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            if (turn != 70) entity.setAnimation("animation.yaoshou026.skill06");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 9) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou014walk"), SoundSource.NEUTRAL, 2.5F, 1.2F);
            data.putString("jinengjinxing", "振翅");
        } else if (phase.equals("振翅") && data.getDouble("entitytick") >= 13) {
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 20,
                        entity.getBbWidth(), 0, entity.getBbWidth(), 0.5);
            }
            double angle = Math.toRadians(entity.getYRot() + turn);
            entity.setDeltaMovement(-Math.sin(angle), 0.6, Math.cos(angle));
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 31) {
            look(entity, target);
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void burst(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return;
        Skills.showWindBurst(entity);
        level.sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY(), entity.getZ(), 40, 0, 0, 0, 1);
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:tongyong_baoqi"), SoundSource.NEUTRAL, 1, 1);
        Vec3 center = entity.position();
        for (Entity victim : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(4.5)).stream()
                .sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
            if (Relations.isFriendly(entity, victim)) continue;
            if (victim.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("yvanchuxiuzhen:noentity")))) {
                level.sendParticles(Particles.type("brokenlightgray"), victim.getX(), victim.getY(), victim.getZ(), 16, 0, 0, 0, 1);
                victim.discard();
            } else if (!hasSuperArmor(victim)) {
                victim.setDeltaMovement(victim.getX() - entity.getX(), 0.5, victim.getZ() - entity.getZ());
            }
        }
        for (int i = 0; i < 20; i++) {
            double angle = i * 18;
            level.sendParticles(ParticleTypes.CLOUD,
                    entity.getX() + entity.getBbWidth() * Math.cos(angle), entity.getY() + entity.getBbHeight() * 0.5,
                    entity.getZ() + entity.getBbWidth() * Math.sin(angle), 0,
                    0.5 * Math.cos(angle), 0, 0.5 * Math.sin(angle), 0.5);
        }
        YaoshouShared.finishSkill(entity);
    }

    private static void flamePalm(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            entity.setAnimation("animation.yaoshou026.skill04");
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 19) {
            Vec3 center = entity.position().add(0, entity.getBbHeight() / 2, 0);
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(2.5))
                    .stream().sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
                if (Relations.isFriendly(entity, victim)) continue;
                particles(entity, victim, Particles.type("cailiao_238particle"), 20, 0.5);
                particles(entity, victim, Particles.type("smallexplosionblue"), 1, 0);
                if (entity.level() instanceof ServerLevel level && victim instanceof LivingEntity living) {
                    SpellDamage.command(level, living, "shanghai 火 " + entity.getStringUUID() + " false 5 1.5");
                }
                if (!hasSuperArmor(victim)) victim.setDeltaMovement(entity.getLookAngle());
            }
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("蓄力") && entity.level() instanceof ServerLevel level) {
            level.sendParticles(Particles.type("cailiao_238particle"), entity.getX(), entity.getY(), entity.getZ(),
                    5, entity.getBbWidth(), 0, entity.getBbWidth(), 0);
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 30) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static void roar(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            Vec3 center = clip(entity, 2 + entity.getBbWidth() / 2);
            if (entity.level() instanceof ServerLevel level) level.sendParticles(Particles.type("whirlwindlightgray"),
                    center.x, center.y, center.z, 1, 0, 0, 0, 0);
            look(entity, target);
            data.putString("jinengjinxing", "蓄力");
            entity.setAnimation("animation.yaoshou026.skill01");
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012skill01"), SoundSource.HOSTILE, 2, 1);
        } else if (phase.equals("蓄力") && data.getDouble("entitytick") >= 20) {
            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou048walk"), SoundSource.NEUTRAL, 1, 1);
            for (int distance = 1; distance <= 5; distance += 2) {
                Vec3 center = clip(entity, distance + entity.getBbWidth() / 2);
                for (Entity victim : entity.level().getEntitiesOfClass(Entity.class,
                        new AABB(center, center).inflate((distance + 2) / 2d)).stream()
                        .sorted(Comparator.comparingDouble(other -> other.distanceToSqr(center))).toList()) {
                    if (Relations.isFriendly(entity, victim)) continue;
                    if (!hasSuperArmor(victim)) {
                        victim.setDeltaMovement(entity.getLookAngle().x, 0.5, entity.getLookAngle().z);
                    }
                    if (victim instanceof LivingEntity living) {
                        double victimLevel = victim.getPersistentData().getDouble("LEVELnumber");
                        double ownLevel = data.getDouble("LEVELnumber");
                        if (ownLevel > victimLevel) {
                            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 20));
                            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, (int) (ownLevel - victimLevel - 1)));
                        } else {
                            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 3));
                        }
                    }
                }
            }
            if (entity.level() instanceof ServerLevel level) {
                for (int i = 0; i < 32; i++) {
                    double vx = entity.getLookAngle().x * Yaoshou037054Support.randomDouble(0.5, 1.5);
                    double vy = entity.getLookAngle().y * Yaoshou037054Support.randomDouble(0.5, 1.5);
                    double vz = entity.getLookAngle().z * Yaoshou037054Support.randomDouble(0.5, 1.5);
                    for (ServerPlayer player : level.players()) {
                        level.sendParticles(player, ParticleTypes.CLOUD, true, entity.getX(),
                                entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 0, vx, vy, vz, 1);
                    }
                }
            }
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 49) {
            YaoshouShared.finishSkill(entity);
        }
    }

    private static boolean hasSuperArmor(Entity entity) {
        return entity instanceof LivingEntity living && StatusEffects.has(living, "skilleffect_08");
    }

    private static Vec3 clip(YaoshouEntity entity, double distance) {
        return Vec3.atLowerCornerOf(entity.level().clip(new ClipContext(entity.getEyePosition(),
                entity.getEyePosition().add(entity.getViewVector(1).scale(distance)), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, entity)).getBlockPos());
    }

    private static void particles(Entity source, Entity target, ParticleOptions type,
            int count, double speed) {
        if (source.level() instanceof ServerLevel level) {
            level.sendParticles(type, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), count,
                    0, 0, 0, speed);
        }
    }

    private static void look(Entity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES,
                new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }
}
