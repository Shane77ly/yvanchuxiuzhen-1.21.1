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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.yvanchuxiuzhen.platform.ModId;
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
 * 免伤判定、闪避、物理伤害公式、吸血、致命伤害。目标版本的 {@link LivingIncomingDamageEvent} 与它在同一位置
 * （减免之前，可取消、可改伤害量）。来源在三处各检查一次符箓复活，这里统一发出 {@link LethalDamageEvent}。
 *
 * <p>来源的疑似错误保持原样（Q30）：没有破甲附魔时破甲系数为 0，受击者的基础物理防御与护甲物理防御不计入；
 * 物理攻击量乘的是受击者的体质修正；暴击值被随后的物理公式覆盖，只留粒子。
 */
final class HurtStage {
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

    static void run(LivingIncomingDamageEvent event, LivingEntity victim, Entity attacker) {
        if (immune(victim, attacker)) {
            event.setCanceled(true);
            return;
        }
        DamageSource source = event.getSource();
        float amount = event.getAmount();
        if (isAny(source, NO_DODGE)) {
            return;
        }
        if (Mth.nextInt(victim.getRandom(), 1, 100) <= Enchants.armorSum(victim, Enchants.key("dodge"))) {
            // 未命中提示的粒子 miss 是自定义粒子，随 particle 迁移。
            event.setCanceled(true);
            return;
        }
        if (!isAny(source, NO_FORMULA) && amount > 0) {
            double settled = settle(victim, attacker, amount);
            if (settled > 0) {
                if (StatusEffects.has(victim, "effect_036") && settled >= victim.getHealth()) {
                    settled = victim.getHealth() - 1;
                }
                event.setAmount((float) settled);
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
                event.setCanceled(true);
            }
        }
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

    /** 物理伤害公式：攻击量² / (攻击量 + (防御 × 破甲系数 + 护甲物理减免²之和) × 体质抗性)，再减玄武岩。 */
    private static double settle(LivingEntity victim, Entity attacker, float amount) {
        // 暴击只剩粒子：来源算出的暴击值随即被下面的结果覆盖（Q30）。
        if (attacker instanceof LivingEntity living && victim.level() instanceof ServerLevel level) {
            int critical = Enchants.mainHand(living, Enchants.key("critical"));
            if (critical != 0 && Math.random() < critical * 0.02) {
                level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + 1, victim.getZ(), 10, 0, 0, 0, 1);
            }
        }
        double defense;
        double resistance;
        double attackScale = 1;
        if (victim instanceof Player player) {
            defense = PlayerAttributes.baseValue(player, Stat.ATTACK_DEF);
            resistance = Cultivation.tizhiDouble(player, "AttackDefCorrectPercentage") + 1;
            attackScale = Cultivation.tizhiDouble(player, "AttackAttCorrectPercentage") + 1;
        } else {
            defense = victim.getPersistentData().getDouble("attack_Def");
            resistance = 1;
        }
        double breakArmor = 0;
        int pojia = attacker instanceof LivingEntity living ? Enchants.mainHand(living, Enchants.key("pojia")) : 0;
        if (pojia != 0) {
            breakArmor = 1 - pojia * 0.1;
        }
        ArmorStatEvent armor = new ArmorStatEvent(victim, "attack_Def");
        NeoForge.EVENT_BUS.post(armor);
        double attack = amount * attackScale;
        double settled = Math.pow(attack, 2) / (attack
                + ((defense + armor.sum()) * breakArmor + Enchants.armorSquares(victim, Enchants.key("attdef"))) * resistance);
        int basalt = Enchants.armorSum(victim, Enchants.key("basalt")) * 2;
        if (Mth.nextDouble(victim.getRandom(), 1, 100) < basalt) {
            settled -= basalt;
        }
        return settled;
    }
}
