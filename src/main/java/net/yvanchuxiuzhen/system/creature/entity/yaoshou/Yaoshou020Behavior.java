package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.Random;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 020号妖兽的分身、临时召唤寿命及来源技能选择。 */
public final class Yaoshou020Behavior {
    private Yaoshou020Behavior() { }

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
                entity.setAnimation("animation.yaoshou020.attacked");
            }
        }
    }

    public static void tick(YaoshouEntity entity) {
        if (!entity.isAlive()) return;
        CompoundTag data = entity.getPersistentData();
        Scoreboard scoreboard = entity.level().getScoreboard();
        Objective objective = scoreboard.getObjective("zhaohuanwu");
        if (objective != null && scoreboard.getOrCreatePlayerScore(entity, objective).get() == 1) {
            if (data.getDouble("daojishi") >= 1) {
                data.putDouble("daojishi", data.getDouble("daojishi") - 1);
            } else {
                if (entity.level() instanceof ServerLevel level) {
                    level.sendParticles(Particles.type("brokendeeporange"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                            entity.getZ(), 24, 0, 0, 0, 1);
                    entity.discard();
                }
            }
        }
        data.putDouble("entitytick", data.getDouble("entitytick") + 1);
        if (data.getString("jineng").isEmpty()
                && !data.getString("gongjizhe").equals(data.getString("linshigongjizhe"))) {
            data.putString("gongjizhe", data.getString("linshigongjizhe"));
        }
        Entity target = target(entity);
        if (target == null) {
            if (data.getDouble("entitytick") >= 100) {
                entity.getNavigation().moveTo(entity.getX() + random(-3, 3), entity.getY(), entity.getZ() + random(-3, 3), 1);
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
                case "靠近" -> Yaoshou019Behavior.approach(entity, target);
                case "挣脱" -> YaoshouShared.escape(entity);
                case "普攻" -> Yaoshou019Behavior.attack(entity, target, "animation.yaoshou020.attack");
                case "分身" -> duplicate(entity, target);
                case "逃跑" -> Yaoshou019Behavior.flee(entity, target, "smokedeeporange");
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
            boolean accelerated = entity.hasEffect(MobEffects.MOVEMENT_SPEED);
            if (!StatusEffects.has(entity, "effect_002") && !StatusEffects.has(entity, "effect_033")) {
                if (!accelerated) candidate(data, ++count, "分身", 10, 20);
                if (distance > 3) {
                    candidate(data, ++count, "逃跑", 5, 30);
                    if (!accelerated) candidate(data, ++count, "靠近", 5, 10);
                } else {
                    if (!accelerated) candidate(data, ++count, "普攻", 10, 20);
                    candidate(data, ++count, "逃跑", 5, 30);
                }
            }
            if (!StatusEffects.has(entity, "effect_033") && distance < 3 && !accelerated) {
                candidate(data, ++count, "普攻", 10, 10);
            }
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

    private static void duplicate(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "准备");
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 40) {
            entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("smokedeeporange"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(), 40, entity.getBbHeight(), entity.getBbHeight(), 0, 1);
                for (int i = 0; i < 2; i++) {
                    YaoshouEntity copy = (YaoshouEntity) entity.getType().create(level);
                    copy.moveTo(entity.getX(), entity.getY(), entity.getZ(), level.getRandom().nextFloat() * 360F, 0);
                    Relations.copyAllegiance(entity, copy);
                    copy.getPersistentData().putDouble("LEVELnumber", Math.ceil(data.getDouble("LEVELnumber") / 2));
                    copy.getPersistentData().putDouble("daojishi", 200);
                    Scoreboard scoreboard = level.getScoreboard();
                    Objective objective = scoreboard.getObjective("zhaohuanwu");
                    if (objective == null) {
                        objective = scoreboard.addObjective("zhaohuanwu", ObjectiveCriteria.DUMMY,
                                Component.literal("zhaohuanwu"), ObjectiveCriteria.RenderType.INTEGER, false, null);
                    }
                    scoreboard.getOrCreatePlayerScore(copy, objective).set(1);
                    copy.getPersistentData().putString("gongjizhe", data.getString("gongjizhe"));
                    copy.getPersistentData().putString("jineng", "逃跑");
                    copy.finalizeSpawn(level, level.getCurrentDifficultyAt(copy.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                    level.addFreshEntity(copy);
                }
            }
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("准备")) {
            if (entity.level() instanceof ServerLevel level) {
                level.sendParticles(Particles.type("starorange"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                        entity.getZ(), 3, entity.getBbHeight(), entity.getBbHeight(), 0, 0);
                level.sendParticles(Particles.type("brokendeeporange"), entity.getX(), entity.getY(), entity.getZ(),
                        1, entity.getBbWidth(), 0, entity.getBbWidth(), 0);
            }
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 41) {
            YaoshouShared.finishSkill(entity);
            data.putString("jineng", "逃跑");
        }
    }

    private static void candidate(CompoundTag data, int index, String skill, int delay, int weight) {
        data.putString("linshijineng" + index, skill);
        data.putDouble("jinengjishi" + index, delay);
        data.putDouble("jinengqvanzhong" + index, weight);
    }

    private static int random(int min, int max) {
        return min >= max ? min : new Random().nextInt(max - min + 1) + min;
    }

    private static Entity target(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try {
            return level.getEntity(UUID.fromString(entity.getPersistentData().getString("gongjizhe")));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static void look(YaoshouEntity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES,
                new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }
}
