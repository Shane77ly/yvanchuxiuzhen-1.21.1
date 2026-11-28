package net.yvanchuxiuzhen.system.combat.damage;

import java.util.Set;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.ArmorStatEvent;
import net.yvanchuxiuzhen.system.combat.api.DamageImmunityEvent;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.LethalDamageEvent;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 受伤处理（来源 {@code EntityBeforeHurt}，1.18.2 版本在 {@code LivingHurtEvent} 中执行，伤害量是护甲等减免之前的值）：
 * 免伤判定、闪避、物理伤害公式、吸血、致命伤害。经原版盾牌处理后、护甲减免前读取伤害容器，
 * 保留来源先盾挡后受伤的顺序。来源在三处各检查一次符箓复活，这里统一发出 {@link LethalDamageEvent}。
 *
 * <p>已按阶段 4 裁定修正 Q30：无破甲附魔时破甲系数为 1（有破甲时为 {@code max(0, 1 − 等级 × 0.1)}）；
 * 物理攻击倍率 {@code AttackAttCorrectPercentage} 改读攻击者体质；暴击倍率写入最终物理攻击量。
 */
public final class HurtStage {
    private static final TagKey<EntityType<?>> NPC = tag("npcentity");
    private static final TagKey<EntityType<?>> OLD_NPC = tag("jiunpc");
    /** 不判闪避的伤害类型。 */
    private static final Set<ResourceKey<DamageType>> NO_DODGE = Set.of(DamageTypes.IN_WALL, DamageTypes.DROWN, DamageTypes.STARVE, DamageTypes.DRY_OUT);
    /** 不套物理公式的伤害类型。 */
    private static final Set<ResourceKey<DamageType>> NO_FORMULA = Set.of(DamageTypes.DRAGON_BREATH, DamageTypes.FREEZE, DamageTypes.IN_FIRE,
            DamageTypes.LIGHTNING_BOLT, DamageTypes.ON_FIRE, DamageTypes.LAVA, DamageTypes.HOT_FLOOR, DamageTypes.FELL_OUT_OF_WORLD,
            DamageTypes.MAGIC, DamageTypes.WITHER);

    private HurtStage() {
    }

    private static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    /** 返回 true 时由调用者清零当前容器并中止本次实际受伤，不撤销已发生的盾挡。 */
    public static boolean runAfterShield(LivingEntity victim, DamageContainer damage) {
        Entity attacker = damage.getSource().getEntity();
        if (attacker == null) {
            return false;
        }
        if (immune(victim, attacker)) {
            return true;
        }
        DamageSource source = damage.getSource();
        // 此处的原值已扣盾挡和无敌帧减量；符箓始终用它比较，不受下方物理公式改写影响。
        float amount = damage.getNewDamage();
        boolean specialSource = isAny(source, NO_DODGE);
        if (!specialSource
                && Mth.nextInt(victim.getRandom(), 1, 100) <= Enchants.armorSum(victim, Enchants.key("dodge"))) {
            // 未命中提示的粒子 miss 是自定义粒子，随 particle 迁移。
            return true;
        }
        if (!specialSource && !isAny(source, NO_FORMULA) && amount > 0) {
            double settled = settle(victim, attacker, amount);
            if (settled > 0) {
                if (StatusEffects.has(victim, "effect_036") && settled >= victim.getHealth()) {
                    settled = victim.getHealth() - 1;
                }
                damage.setNewDamage(Math.max(0, (float) settled));
            }
            if (attacker instanceof LivingEntity living) {
                int vampire = Enchants.mainHand(living, Enchants.key("vampire"));
                if (vampire != 0) {
                    living.setHealth((float) (living.getHealth() + settled * vampire * 5 * 0.01));
                }
            }
        }
        if (victim.getHealth() < amount) {
            LethalDamageEvent lethal = new LethalDamageEvent(victim, amount);
            NeoForge.EVENT_BUS.post(lethal);
            if (lethal.survived()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAny(DamageSource source, Set<ResourceKey<DamageType>> types) {
        return types.stream().anyMatch(source::is);
    }

    /** 敌友：攻击者与受击者是否玩家四种组合各取友方集合与团队（来源把受击玩家的数据来源与攻击者的混用，不会判为友方）。 */
    private static boolean immune(LivingEntity victim, Entity attacker) {
        if (victim.getType().is(NPC) || victim.getType().is(OLD_NPC)) {
            return true;
        }
        boolean victimTeam = !Relations.team(victim).isEmpty();
        if (attacker instanceof Player) {
            return victimTeam && Relations.isFriendly(attacker, victim) && (victim instanceof Player || !Relations.team(attacker).isEmpty());
        }
        boolean friendly = !(victim instanceof Player) && victimTeam && !Relations.team(attacker).isEmpty() && Relations.isFriendly(victim, attacker);
        if (friendly) {
            return true;
        }
        DamageImmunityEvent event = new DamageImmunityEvent(victim, attacker);
        NeoForge.EVENT_BUS.post(event);
        return event.immune();
    }

    /** 物理伤害公式：(攻击量 × 暴击倍率 × 攻击者体质修正)² / (前项 + (防御 × 破甲系数 + 护甲物理减免²之和) × 体质抗性)，再减玄武岩。 */
    private static double settle(LivingEntity victim, Entity attacker, float amount) {
        double critMultiplier = 1;
        if (attacker instanceof LivingEntity living && victim.level() instanceof ServerLevel level) {
            int critical = Enchants.mainHand(living, Enchants.key("critical"));
            if (critical != 0 && Math.random() < critical * 0.02) {
                level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1, victim.getZ(), 10, 0, 0, 0, 1);
                critMultiplier = 1.5 + Enchants.mainHand(living, Enchants.key("criticalatt")) * 0.2;
            }
        }
        double defense;
        double resistance;
        if (victim instanceof Player player) {
            defense = PlayerAttributes.baseValue(player, Stat.ATTACK_DEF);
            resistance = Cultivation.tizhiDouble(player, "AttackDefCorrectPercentage") + 1;
        } else {
            defense = EntityAttributes.stat(victim, Stat.ATTACK_DEF);
            resistance = 1;
        }
        double attackScale = attacker instanceof Player attackerPlayer
                ? Cultivation.tizhiDouble(attackerPlayer, "AttackAttCorrectPercentage") + 1
                : 1;
        int pojia = attacker instanceof LivingEntity living ? Enchants.mainHand(living, Enchants.key("pojia")) : 0;
        double breakArmor = Math.max(0, 1 - pojia * 0.1);
        ArmorStatEvent armor = new ArmorStatEvent(victim, "attack_Def");
        NeoForge.EVENT_BUS.post(armor);
        double attack = amount * critMultiplier * attackScale;
        double settled = Math.pow(attack, 2) / (attack
                + ((defense + armor.sum()) * breakArmor + Enchants.armorSquares(victim, Enchants.key("attdef"))) * resistance);
        int basalt = Enchants.armorSum(victim, Enchants.key("basalt")) * 2;
        if (Mth.nextDouble(victim.getRandom(), 1, 100) < basalt) {
            settled -= basalt;
        }
        return settled;
    }
}
