package net.yvanchuxiuzhen.system.combat.damage;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/**
 * 命中时的武器与护甲附魔（来源 {@code EntityHurt2}、{@code EntityHurt3}），在 {@link AttackStage} 中攻击者与受击者不友好时执行。
 * 护甲附魔读受击者四件护甲的等级之和，武器附魔读攻击者主手；范围技对以受击者为中心边长 5 的方体内的非友好生物发
 * {@link Shanghai} 伤害（来源经 {@code /shanghai} 命令，攻击者为附魔所有者，基础量按 {@code DecimalFormat("0")} 取整）。
 *
 * <p>召唤其他系统实体的三个附魔不在这里：{@code qishi}（召唤 skill 的阴灵）、{@code lingwen}（召唤 skill 的灵纹）、
 * {@code starsky}（发射 fulu 的星空箭）由对应系统订阅 {@link net.yvanchuxiuzhen.system.combat.api.HitEvent} 执行。
 * 粒子中带 {@code SMALLEXPLOSION*}、{@code FLASH_ICE}、{@code THUNDERDEEPPURPLE}、{@code STARLIGHTBLUE}、
 * {@code SMOKEDEEPORANGE} 的是自定义粒子，随 particle 迁移。来源的疑似错误保持原样（Q32）：
 * {@code lingtikiller} 取 {@code zhongshang} 的等级，{@code liuqing} 的时长使用 {@code iceexplosion} 分支才赋值的等级，
 * {@code chaos} 的范围中心把视线方向的 z 分量当作 z 坐标。
 */
final class HitEnchantments {
    private static final double AREA = 5 / 2d;

    private HitEnchantments() {
    }

    static void run(ServerLevel level, LivingEntity victim, LivingEntity attacker) {
        armor(level, victim, attacker);
        weapon(level, victim, attacker);
        iceExplosion(level, victim, attacker);
        if (Enchants.mainHand(attacker, Enchants.key("liuqing")) != 0 && victim.isAlive()) {
            int mercy = Enchants.mainHand(attacker, Enchants.key("iceexplosion"));
            victim.addEffect(new MobEffectInstance(StatusEffects.holder("effect_036"), mercy * 40, 0));
        }
        control(victim, attacker, "threaten", 10, null);
        control(victim, attacker, "nofly", 40, "effect_035");
        chaos(level, victim, attacker);
        int pull = Enchants.mainHand(attacker, Enchants.key("thegreatvoid"));
        if (pull != 0 && !Relations.isFriendly(attacker, victim)) {
            victim.setDeltaMovement((attacker.getX() - victim.getX()) * pull * 0.2, 0.5, (attacker.getZ() - victim.getZ()) * pull * 0.2);
        }
    }

    /** 防具附魔：烈焰甲、寒冰甲、放电、池鱼。 */
    private static void armor(ServerLevel level, LivingEntity wearer, LivingEntity attacker) {
        int fire = Enchants.armorSum(wearer, Enchants.key("firearmor"));
        if (fire != 0 && Mth.nextDouble(wearer.getRandom(), 1, 100) < fire) {
            level.sendParticles(ParticleTypes.FLAME, wearer.getX(), wearer.getY() + 1, wearer.getZ(), 20, 0, 0, 0, 1);
            level.sendParticles(ParticleTypes.FLAME, wearer.getX(), wearer.getY() + 0.3, wearer.getZ(), 20, 1.5, 0, 1.5, 0);
            for (LivingEntity target : around(level, wearer.getX(), wearer.getY() + 1, wearer.getZ())) {
                if (!Relations.isFriendly(wearer, target)) {
                    Shanghai.deal(level, target, new Shanghai.Request(DamageCategory.FIRE, wearer.getUUID(), true, fire * 2, 1));
                }
            }
        }
        int ice = Enchants.armorSum(wearer, Enchants.key("icearmor"));
        if (ice != 0 && Mth.nextDouble(wearer.getRandom(), 1, 100) < ice) {
            level.sendParticles(ParticleTypes.CLOUD, wearer.getX(), wearer.getY() + 1, wearer.getZ(), 20, 0, 0, 0, 1);
            for (LivingEntity target : around(level, wearer.getX(), wearer.getY() + 1, wearer.getZ())) {
                if (!Relations.isFriendly(wearer, target)) {
                    Shanghai.deal(level, target, new Shanghai.Request(DamageCategory.ICE, wearer.getUUID(), true, ice * 2, 1));
                    target.setTicksFrozen(target.getTicksFrozen() + 10);
                }
            }
        }
        int discharge = Enchants.armorSum(wearer, Enchants.key("discharge"));
        if (discharge != 0 && Mth.nextDouble(wearer.getRandom(), 1, 100) < discharge) {
            for (LivingEntity target : around(level, wearer.getX(), wearer.getY() + 1, wearer.getZ())) {
                if (!Relations.isFriendly(wearer, target)) {
                    Shanghai.deal(level, target, new Shanghai.Request(DamageCategory.THUNDER, wearer.getUUID(), true, discharge * 2, 1));
                }
            }
        }
        int pool = Enchants.armorSum(wearer, Enchants.key("pool"));
        if (pool != 0 && Mth.nextDouble(wearer.getRandom(), 1, 100) < pool && !Relations.isFriendly(attacker, wearer)) {
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, pool - 1));
        }
    }

    /** 武器附魔：流焰、破甲标记、眩晕、沉默、灵体杀手、剧痛、重伤、禁锢。 */
    private static void weapon(ServerLevel level, LivingEntity victim, LivingEntity attacker) {
        int burning = Enchants.mainHand(attacker, Enchants.key("liuyan"));
        if (burning != 0 && Mth.nextInt(attacker.getRandom(), 1, 100) < burning) {
            level.sendParticles(ParticleTypes.FLAME, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 10, 0, 0, 0, 0.5);
            burst(level, victim, attacker, DamageCategory.FIRE, burning, ParticleTypes.FLAME);
        }
        if (Enchants.mainHand(attacker, Enchants.key("pojia")) != 0 && victim.isAlive() && victim.getPersistentData().getDouble("caijinumber") < 1) {
            victim.getPersistentData().putDouble("caijinumber", 1);
        }
        control(victim, attacker, "yvnxvan", 10, "effect_001");
        control(victim, attacker, "chenmo", 10, "effect_002");
        int killer = Enchants.mainHand(attacker, Enchants.key("lingtikiller"));
        if (killer != 0 && !Relations.isFriendly(attacker, victim) && victim.isAlive()) {
            victim.setHealth((float) (victim.getHealth() - Enchants.mainHand(attacker, Enchants.key("zhongshang")) * 2));
        }
        control(victim, attacker, "jvtong", 10, "effect_003");
        control(victim, attacker, "zhongshang", 10, "effect_007");
        int confine = Enchants.mainHand(attacker, Enchants.key("confine"));
        if (confine != 0 && !Relations.isFriendly(attacker, victim) && victim.isAlive() && Mth.nextDouble(attacker.getRandom(), 1, 100) < confine * 2) {
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, confine * 10, 10));
        }
    }

    private static void iceExplosion(ServerLevel level, LivingEntity victim, LivingEntity attacker) {
        int explosion = Enchants.mainHand(attacker, Enchants.key("iceexplosion"));
        if (explosion != 0 && Mth.nextInt(attacker.getRandom(), 1, 100) < explosion) {
            burst(level, victim, attacker, DamageCategory.ICE, explosion, ParticleTypes.CLOUD);
        }
    }

    /** 概率 等级 × 2%：按附魔等级给受击者施加控制效果，{@code effect} 为空时是原版失明；时长 等级 × {@code unit} 刻，等级为附魔等级。 */
    private static void control(LivingEntity victim, LivingEntity attacker, String enchantment, int unit, String effect) {
        int level = Enchants.mainHand(attacker, Enchants.key(enchantment));
        if (level != 0 && !Relations.isFriendly(attacker, victim) && victim.isAlive() && Mth.nextDouble(attacker.getRandom(), 1, 100) < level * 2) {
            MobEffectInstance instance = effect == null ? new MobEffectInstance(MobEffects.BLINDNESS, level * unit, level)
                    : new MobEffectInstance(StatusEffects.holder(effect), level * unit, level);
            victim.addEffect(instance);
        }
    }

    /** 混沌气息：范围中心的 z 取受击者视线方向的 z 分量（来源如此，Q32），范围内非友好生物虚弱并禁空。 */
    private static void chaos(ServerLevel level, LivingEntity victim, LivingEntity attacker) {
        int chaos = Enchants.mainHand(attacker, Enchants.key("chaos"));
        if (chaos == 0) {
            return;
        }
        for (LivingEntity target : around(level, victim.getX(), victim.getY(), victim.getLookAngle().z)) {
            if (!Relations.isFriendly(attacker, target)) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, chaos * 10, chaos));
                target.addEffect(new MobEffectInstance(StatusEffects.holder("effect_035"), chaos * 10, chaos));
            }
        }
    }

    /** 以受击者为中心的范围爆发：范围内非友好生物受 等级 × 4、倍率 0.2 的完整结算，并围成一圈粒子。 */
    private static void burst(ServerLevel level, LivingEntity victim, LivingEntity attacker, DamageCategory category, int enchantLevel, ParticleOptions ring) {
        for (LivingEntity target : around(level, victim.getX(), victim.getY(), victim.getZ())) {
            if (!Relations.isFriendly(attacker, target)) {
                Shanghai.deal(level, target, new Shanghai.Request(category, attacker.getUUID(), false, enchantLevel * 4, 0.2));
            }
        }
        // 来源把 360 / 粒子数 = 18 当作弧度递增。
        double angle = 0;
        for (int i = 0; i < 20; i++) {
            double speed = 0.5;
            level.sendParticles(ring, victim.getX() + 2 * Math.cos(angle), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ() + 2 * Math.sin(angle),
                    0, speed * Math.cos(angle), 0, speed * Math.sin(angle), speed);
            angle += 360 / 20.0;
        }
    }

    private static Iterable<LivingEntity> around(ServerLevel level, double x, double y, double z) {
        Vec3 center = new Vec3(x, y, z);
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(AREA), Entity::isAlive);
    }
}
