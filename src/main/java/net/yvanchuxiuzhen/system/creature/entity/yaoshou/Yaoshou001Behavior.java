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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;

/** 001号妖兽的受击目标、加权选招与完整技能阶段。 */
public final class Yaoshou001Behavior {
    private Yaoshou001Behavior() { }

    public static void hurt(YaoshouEntity entity, DamageSource source) {
        hurt(entity, source, "animation.yaoshou001.attacked");
    }

    static void hurt(YaoshouEntity entity, DamageSource source, String attackedAnimation) {
        Entity attacker = source.getEntity();
        if (entity.isAlive() && attacker != null && !YaoshouShared.isFriendly(entity, attacker)) {
            CompoundTag data = entity.getPersistentData();
            data.putString("linshigongjizhe", attacker.getStringUUID());
            if (data.getBoolean("beidaduan")) {
                entity.setAnimation("empty");
                data.putBoolean("beidaduan", false);
            }
            if (data.getString("jineng").isEmpty() && !entity.level().isClientSide()) {
                entity.setAnimation(attackedAnimation);
            }
        }
    }

    public static void tick(YaoshouEntity entity) {
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
        } else if (!target.isAlive()) {
            data.putString("gongjizhe", "");
            data.putString("linshigongjizhe", "");
            YaoshouShared.finishSkill(entity);
        } else if (data.getString("jineng").isEmpty()) {
            if (data.getString("yvdingjineng").isEmpty()) schedule(entity, target);
            else beginScheduled(entity);
        } else {
            switch (data.getString("jineng")) {
                case "靠近" -> approach(entity, target);
                case "挣脱" -> YaoshouShared.escape(entity);
                case "普攻" -> attack(entity, target, "animation.yaoshou001.attack");
                case "增幅法攻" -> amplify(entity);
                case "冲刺" -> sprint(entity, target);
                case "扎根" -> root(entity);
                case "花粉散播" -> pollen(entity, target, "animation.yaoshou001.skill01");
                case "鞭挞" -> whip(entity, target, "animation.yaoshou001.skill03");
                default -> { }
            }
        }
    }

    private static void schedule(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        double distance = entity.distanceTo(target) - (entity.getBbWidth() / 2 + target.getBbWidth() / 2);
        look(entity, target);
        int count = 0;
        if (entity.isPassenger()) candidate(data, ++count, "挣脱", 10, 100);
        else {
            if (!entity.hasEffect(StatusEffects.holder("effect_002")) && !entity.hasEffect(StatusEffects.holder("effect_033"))) {
                if (distance > 8) {
                    candidate(data, ++count, "增幅法攻", 40, 30);
                    candidate(data, ++count, "冲刺", 20, 60);
                } else if (distance >= 2) {
                    if (entity.getHealth() < entity.getMaxHealth() / 2) candidate(data, ++count, "扎根", 20, 50);
                    candidate(data, ++count, "靠近", 10, Math.round(distance * 10));
                    candidate(data, ++count, "鞭挞", 20, 70);
                } else {
                    candidate(data, ++count, "花粉散播", 40, 30);
                    candidate(data, ++count, "鞭挞", 20, 40);
                }
            }
            if (!entity.hasEffect(StatusEffects.holder("effect_033")) && distance < 2) candidate(data, ++count, "普攻", 20, 30);
        }
        choose(data, count);
    }

    static void beginScheduled(YaoshouEntity entity) {
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

    static void candidate(CompoundTag data, int index, String skill, int delay, double weight) {
        data.putString("linshijineng" + index, skill);
        data.putDouble("jinengjishi" + index, delay);
        data.putDouble("jinengqvanzhong" + index, weight);
    }

    static void choose(CompoundTag data, int count) {
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

    static void approach(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        if (data.getString("jinengjinxing").isEmpty()) {
            look(entity, target);
            entity.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), 1.5);
            data.putString("jinengjinxing", "移动中");
        } else if (data.getString("jinengjinxing").equals("移动中") && data.getDouble("entitytick") >= 40) YaoshouShared.finishSkill(entity);
    }

    static void attack(YaoshouEntity entity, Entity target, String animation) {
        CompoundTag data = entity.getPersistentData();
        if (data.getString("jinengjinxing").isEmpty()) {
            data.putString("jinengjinxing", "准备");
            animate(entity, animation);
            look(entity, target);
        } else if (data.getString("jinengjinxing").equals("准备") && data.getDouble("entitytick") >= 5) {
            Vec3 center = Vec3.atLowerCornerOf(trace(entity, 2 + entity.getBbWidth() / 2));
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(1.5), candidate -> true)
                    .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (YaoshouShared.isFriendly(entity, victim)) continue;
                if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 5, 0, 0, 0, 0.5);
                double damage = data.getDouble("attack_Num");
                if (entity.hasEffect(MobEffects.WEAKNESS)) damage -= (entity.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1) * 4;
                if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) damage += (entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1) * 3;
                victim.hurt(entity.damageSources().source(DamageTypes.PLAYER_ATTACK, entity), (float) (damage <= 0 ? 0.1 : damage));
            }
            data.putString("jinengjinxing", "攻击");
        } else if (data.getString("jinengjinxing").equals("攻击") && data.getDouble("entitytick") >= 11) YaoshouShared.finishSkill(entity);
    }

    private static void amplify(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            animate(entity, "animation.yaoshou001.skill01");
            data.putString("jinengjinxing", "吸收开始");
        } else if (phase.equals("吸收开始") && data.getDouble("entitytick") >= 12) {
            for (int i = 0; i < 6; i++) SkillDelays.after(2 + i * 5, () -> {
                particles(entity, "striplightblue", 5, entity.getBbWidth(), 0, entity.getBbWidth(), 0, 0);
                particles(entity, "stripblue", 10, entity.getBbWidth(), 0, entity.getBbWidth(), 0, 0);
                particles(entity, "starlightblue", 8, entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth(), 0, entity.getBbHeight() / 2);
            });
            entity.addEffect(new MobEffectInstance(StatusEffects.holder("effect_013"), 400, (int) Math.floor(data.getDouble("LEVELnumber") / 10)));
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 28) YaoshouShared.finishSkill(entity);
    }

    private static void sprint(YaoshouEntity entity, Entity target) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putString("jinengjinxing", "起飞");
            entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 1, false, false));
        } else if (phase.equals("起飞") && data.getDouble("entitytick") >= 20) {
            animate(entity, "animation.yaoshou001.sprint");
            look(entity, target);
            entity.setDeltaMovement(entity.getLookAngle().x, 0.2, entity.getLookAngle().z);
            data.putString("jinengjinxing", "冲刺");
        } else if (phase.equals("冲刺") && data.getDouble("entitytick") >= 40) YaoshouShared.finishSkill(entity);
    }

    private static void root(YaoshouEntity entity) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            animate(entity, "animation.yaoshou001.skill02");
            data.putString("jinengjinxing", "吸收开始");
        } else if (phase.equals("吸收开始") && data.getDouble("entitytick") >= 28) {
            data.putDouble("entitytick2", data.getDouble("entitytick2") + 1);
            if (data.getDouble("entitytick2") >= 20) {
                if (entity.level() instanceof ServerLevel level) level.sendParticles(ParticleTypes.HAPPY_VILLAGER, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 8, 0.2, 1, 0.2, 0);
                entity.setHealth((float) (entity.getHealth() + data.getDouble("mana_Att") / 2));
                data.putDouble("entitytick2", 0);
            }
            if (data.getDouble("entitytick") >= 264) data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 274) YaoshouShared.finishSkill(entity);
    }

    static void pollen(YaoshouEntity entity, Entity target, String animation) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "释放");
            animate(entity, animation);
        } else if (phase.equals("释放") && data.getDouble("entitytick") >= 12) {
            particles(entity, "peachflowerlizi", 30, 4, 1, 4, 0, entity.getBbHeight());
            particles(entity, "smokelightred", 20, 4, 1, 4, 0.1, entity.getBbHeight());
            Vec3 center = entity.position();
            for (Entity victim : entity.level().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(5), candidate -> true)
                    .stream().sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
                if (!YaoshouShared.isFriendly(entity, victim) && victim instanceof LivingEntity living) living.addEffect(new MobEffectInstance(StatusEffects.holder("duyao_01"), 200, 0));
            }
            data.putString("jinengjinxing", "结束");
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 28) YaoshouShared.finishSkill(entity);
    }

    static void whip(YaoshouEntity entity, Entity target, String animation) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            look(entity, target);
            data.putString("jinengjinxing", "释放");
            animate(entity, animation);
        } else if (phase.equals("释放") && data.getDouble("entitytick") >= 20) {
            data.putString("jinengjinxing", "结束");
            if (entity.level() instanceof ServerLevel level) {
                BlockPos position = trace(entity, 1 + entity.getBbWidth() / 2);
                SkillSummons.summonVineWhip(level, entity, Vec3.atLowerCornerOf(position));
            }
        } else if (phase.equals("结束") && data.getDouble("entitytick") >= 40) YaoshouShared.finishSkill(entity);
    }

    static void animate(YaoshouEntity entity, String animation) {
        if (!entity.level().isClientSide()) entity.setAnimation(animation);
    }

    static Entity target(YaoshouEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) return null;
        try { return level.getEntity(UUID.fromString(entity.getPersistentData().getString("gongjizhe"))); }
        catch (IllegalArgumentException exception) { return null; }
    }

    static BlockPos trace(YaoshouEntity entity, double distance) {
        return entity.level().clip(new ClipContext(entity.getEyePosition(1), entity.getEyePosition(1).add(entity.getViewVector(1).scale(distance)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity)).getBlockPos();
    }

    static void particles(YaoshouEntity entity, String id, int count, double dx, double dy, double dz, double speed, double height) {
        if (entity.level() instanceof ServerLevel level) level.sendParticles(Particles.type(id), entity.getX(), entity.getY() + height, entity.getZ(), count, dx, dy, dz, speed);
    }

    static int random(int min, int max) { return min >= max ? min : new Random().nextInt(max - min + 1) + min; }
    static void look(YaoshouEntity entity, Entity target) {
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, new Vec3(target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ()));
    }
}
