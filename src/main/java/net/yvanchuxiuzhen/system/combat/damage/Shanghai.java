package net.yvanchuxiuzhen.system.combat.damage;

import java.util.Locale;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.ArmorStatEvent;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.DamageImmunityEvent;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.LethalDamageEvent;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 灵力与神识伤害的结算（来源 {@code ShanghaiGo}、{@code Zhilingdamage}、{@code Zhilingdefence}），其他系统以
 * {@link Request} 造成这类伤害，来源中经 {@code /shanghai} 命令发起的 209 处调用都对应一次 {@link #deal}。
 *
 * <p>流程：敌友与免伤 → 闪避 → 攻击量（非固定且有来源时按攻击方属性、五行、功法、附魔计算）→ 防御 → 总量
 * （神识类 {@code 攻击 − 防御}，其余 {@code 攻击² / (攻击 + 防御)}）→ 手下留情限制 → 以 {@code linglidamage} 或
 * {@code jinglidamage}（绕过护甲）扣血。来源已知的疑似错误保持原样（见战斗架构 Q29、Q30）。
 */
public final class Shanghai {
    private Shanghai() {
    }

    /**
     * 一次伤害请求。{@code attacker} 为空表示无来源（命令里写“无”）；{@code fixed} 为真时攻击量就是 {@code base}，
     * 不再叠加攻击方的属性；{@code multiplier} 是攻击量的倍率基数。
     */
    public record Request(DamageCategory category, UUID attacker, boolean fixed, double base, double multiplier) {
    }

    public static void deal(ServerLevel level, LivingEntity target, Request request) {
        if (!target.isAlive() || !(target instanceof Mob || target instanceof Player) || request.base() <= 0) {
            return;
        }
        Entity attacker = null;
        if (request.attacker() != null) {
            attacker = level.getEntity(request.attacker());
            if (attacker == null) {
                // 来源在这里对空来源读取玩家变量而抛出异常，命令失败，没有伤害。
                return;
            }
            if (friendly(target, attacker)) {
                return;
            }
        }
        // 来源把 0–100 的随机数写进目标的记分板 suijishu 再读回，只是借用，这里直接用局部随机数。
        if (Mth.nextInt(target.getRandom(), 0, 100) <= Enchants.armorSum(target, Enchants.key("dodge"))) {
            return;
        }
        DamageCategory category = request.category();
        double damage = request.base();
        if (attacker != null && !request.fixed()) {
            damage = attack(level, target, attacker, request);
        }
        double defence = defence(level, target, category);
        double total = category.isSoul() ? damage - defence : Math.pow(damage, 2) / (damage + defence);
        if (StatusEffects.has(target, "effect_036") && total >= target.getHealth()) {
            total = target.getHealth() - 1;
        }
        if (!(total > 0)) {
            return;
        }
        DamageSource source = CombatDamageTypes.source(level, category.isSoul(), attacker);
        if (target.getHealth() < total) {
            LethalDamageEvent lethal = new LethalDamageEvent(target, total);
            NeoForge.EVENT_BUS.post(lethal);
            if (lethal.survived()) {
                return;
            }
            target.hurt(source, (float) total);
            if (attacker instanceof ServerPlayer player) {
                recordKill(player, target, category);
            }
        } else {
            target.hurt(source, (float) total);
        }
    }

    /** 攻击方是否把受击方视为友方，按攻击方与受击方是否玩家四种组合取友方集合与团队，见战斗架构。 */
    private static boolean friendly(LivingEntity target, Entity attacker) {
        if (attacker instanceof Player) {
            boolean listed = !Relations.team(target).isEmpty() && Relations.isFriendly(attacker, target);
            if (target instanceof Player) {
                return listed;
            }
            return listed || immune(target, attacker);
        }
        if (target instanceof Player) {
            // 来源读受击玩家持久化数据里的友方集合与攻击者的玩家变量团队，两者都是空，不会判为友方。
            return false;
        }
        return (!Relations.team(attacker).isEmpty() && Relations.isFriendly(target, attacker)) || immune(target, attacker);
    }

    private static boolean immune(LivingEntity target, Entity attacker) {
        DamageImmunityEvent event = new DamageImmunityEvent(target, attacker);
        NeoForge.EVENT_BUS.post(event);
        return event.immune();
    }

    /** {@code Zhilingdamage}：攻击量 = (基础量 + 各项加成) × 倍率 + 功法伤害，附魔暴击再乘。 */
    private static double attack(ServerLevel level, LivingEntity target, Entity attacker, Request request) {
        DamageCategory category = request.category();
        double base = request.base();
        double multiplier = request.multiplier();
        double techniqueDamage = 0;
        if (attacker instanceof Player player) {
            if (category.isSoul()) {
                base += PlayerAttributes.value(player, Stat.SOUL_ATT);
            } else {
                base = (base + PlayerAttributes.value(player, Stat.MANA_ATT)) * (Cultivation.tizhiDouble(player, "ManaattAttCorrectPercentage") + 1);
            }
            if (category.element() != null) {
                multiplier += PlayerAttributes.elementCoefficient(player, category.element());
                techniqueDamage = PlayerAttributes.receivedElementBonus(player, category.element());
                String prefix = category.tizhiPrefix();
                base = (base + Cultivation.tizhiDouble(player, prefix + "AttCorrect")) * (Cultivation.tizhiDouble(player, prefix + "AttCorrectPercentage") + 1);
            } else if (category == DamageCategory.SOUND) {
                multiplier += Cultivation.tizhiDouble(player, "SoulattofyinAttCorrectPercentage");
            } else if (category == DamageCategory.SOUL) {
                multiplier += Cultivation.tizhiDouble(player, "SoulattofjingAttCorrectPercentage");
            }
        } else if (attacker instanceof Mob) {
            base += attacker.getPersistentData().getDouble(category.isSoul() ? "soul_Att" : "mana_Att");
        }
        if (attacker instanceof LivingEntity living) {
            for (String enchantment : category.attackEnchantments()) {
                int enchantLevel = Enchants.mainHand(living, Enchants.key(enchantment));
                if (enchantLevel != 0) {
                    base += Math.pow(enchantLevel, category.attackExponent());
                }
            }
            if (category == DamageCategory.FIRE && StatusEffects.has(target, "duyao_16")) {
                base += (StatusEffects.amplifier(target, "duyao_16") + 1) * 10;
            }
            if (category.family() != null) {
                multiplier += Restraint.adjustment(category.family(), target);
            }
            if (category.isSoul()) {
                base += Math.pow(Enchants.mainHand(living, Enchants.key("soulatt")), 2);
                if (StatusEffects.has(living, "effect_020")) {
                    base += Math.pow(StatusEffects.amplifier(living, "effect_020") + 1, 2);
                }
            } else {
                base += Enchants.mainHand(living, Enchants.key("manaatt")) * 2;
                if (StatusEffects.has(living, "effect_013")) {
                    base += Math.pow(StatusEffects.amplifier(living, "effect_013") + 1, 2);
                }
            }
        }
        if (multiplier <= 0) {
            multiplier = 0.1;
        }
        base = base * multiplier + techniqueDamage;
        if (attacker instanceof LivingEntity living) {
            int critical = Enchants.mainHand(living, Enchants.key("critical"));
            if (critical != 0 && Mth.nextInt(target.getRandom(), 0, 100) < critical * 2) {
                level.sendParticles(ParticleTypes.ENCHANTED_HIT, target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 10,
                        target.getBbWidth(), target.getBbHeight(), target.getBbWidth(), 1);
                base *= 1.5 + Enchants.mainHand(living, Enchants.key("criticalatt")) * 0.2;
            }
        }
        return base;
    }

    /**
     * {@code Zhilingdefence}：护甲附魔 + 受击方的防御属性 + 护甲炼器数值 + 玄武岩。
     * 来源中体质对防御的修正读取的是从未赋值的空物品，不生效（Q29），所以玩家只加防御属性的最终值；
     * 法术防御的护甲项读取 {@code mana_Att} 而不是 {@code mana_Def}（Q30）。
     */
    private static double defence(ServerLevel level, LivingEntity target, DamageCategory category) {
        double defence = 0;
        for (DamageCategory.Term term : category.defenseSums()) {
            defence += Enchants.armorSum(target, Enchants.key(term.enchantment())) * term.factor();
        }
        for (String enchantment : category.defenseSquares()) {
            defence += Enchants.armorSquares(target, Enchants.key(enchantment));
        }
        if (category == DamageCategory.THUNDER && StatusEffects.has(target, "effect_022")) {
            defence += Math.pow(StatusEffects.amplifier(target, "effect_022") + 1, 2);
        }
        if (category.isSoul()) {
            if (target instanceof Player player) {
                defence += PlayerAttributes.value(player, Stat.SOUL_DEF);
            } else if (target instanceof Mob) {
                defence += target.getPersistentData().getDouble("soul_Def");
            }
            defence += armorStat(target, "soul_Def");
        } else {
            if (target instanceof Player player) {
                defence += PlayerAttributes.value(player, Stat.MANA_DEF);
            } else if (target instanceof Mob) {
                defence += target.getPersistentData().getDouble("mana_Def");
            }
            defence += armorStat(target, "mana_Att");
            defence += Enchants.armorSquares(target, Enchants.key("manadef"));
        }
        int basalt = Enchants.armorSum(target, Enchants.key("basalt"));
        if (basalt != 0 && Mth.nextInt(target.getRandom(), 1, 100) <= basalt) {
            level.playSound(null, target.getX(), target.getY() + 1, target.getZ(),
                    BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "basalt")), SoundSource.NEUTRAL, 1, 1);
            defence += basalt * 2;
        }
        return defence;
    }

    private static double armorStat(LivingEntity entity, String key) {
        ArmorStatEvent event = new ArmorStatEvent(entity, key);
        NeoForge.EVENT_BUS.post(event);
        return event.sum();
    }

    /**
     * 击杀统计：受击方当前生命低于伤害总量时（不等于确实死亡，Q13），攻击方玩家的体质对应五行的击杀数加 1，
     * 击杀积分加受击方的境界层数（玩家）或持久化 {@code LEVELnumber}（其他生物）。只统计金水木火土。
     */
    private static void recordKill(ServerPlayer player, LivingEntity target, DamageCategory category) {
        String element = switch (category) {
            case GOLD, WATER, TREE, FIRE, SOIL -> category.name().toLowerCase(Locale.ROOT);
            default -> null;
        };
        if (element == null) {
            return;
        }
        Cultivation.setTizhiDouble(player, "killentity" + element, Cultivation.tizhiDouble(player, "killentity" + element) + 1);
        double points = target instanceof Player victim ? Cultivation.get(victim, CultivationValue.LEVEL_NUMBER)
                : target.getPersistentData().getDouble("LEVELnumber");
        Cultivation.setTizhiDouble(player, "killentitypoints" + element, Cultivation.tizhiDouble(player, "killentitypoints" + element) + points);
    }
}
