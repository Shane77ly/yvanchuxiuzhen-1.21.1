package net.yvanchuxiuzhen.system.combat.damage;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.FightingState;
import net.yvanchuxiuzhen.system.combat.api.HitEvent;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 命中处理（来源 {@code EntityHurt}，1.18.2 版本在 {@code LivingAttackEvent} 中执行，伤害量是未经减免的原值）：
 * 受击者的毒厄丹扣血；攻击者与受击者不友好时记录战斗状态、按攻击者主手武器的附魔触发命中效果、
 * 发出 {@link HitEvent}、重击上僵直、妖兽与修者的逃跑层数、驯服宠物的攻击目标。
 */
final class AttackStage {
    private static final TagKey<EntityType<?>> YAOSHOU = tag("yaoshou");
    private static final TagKey<EntityType<?>> XIUZHE = tag("xiuzhe");

    /** 毒药附魔与它施加的毒药效果：概率 等级 × 1%，时长 已有剩余刻数 × 20 + 600，等级为附魔等级 − 1。 */
    private static final List<String[]> POISONS = List.of(
            new String[] {"beepoison", "duyao_10"}, new String[] {"firepoison", "duyao_16"}, new String[] {"redrefining", "duyao_13"},
            new String[] {"spore", "duyao_17"}, new String[] {"sinomeninevine", "duyao_11"}, new String[] {"coldtoxin", "duyao_12"},
            new String[] {"cautery", "duyao_14"}, new String[] {"snakevenom", "duyao_15"});

    private AttackStage() {
    }

    private static TagKey<EntityType<?>> tag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }

    static void run(LivingEntity victim, Entity attacker, double amount) {
        if (StatusEffects.has(victim, "duyao_19")) {
            victim.setHealth((float) (victim.getHealth() - (StatusEffects.amplifier(victim, "duyao_19") + 1)));
            if (victim.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() / 2, victim.getZ(), 10, 0, 0, 0, 1);
            }
        }
        if (Relations.isFriendly(attacker, victim)) {
            return;
        }
        FightingState.mark(victim);
        if (attacker instanceof LivingEntity living) {
            enchantments(victim, living, amount);
        }
        boolean heavy = amount >= victim.getMaxHealth() / 16;
        NeoForge.EVENT_BUS.post(new HitEvent(victim, attacker, amount, heavy));
        if (heavy) {
            victim.addEffect(new MobEffectInstance(StatusEffects.holder("effect_033"), 10, 0, false, false));
        }
        if (victim.getType().is(YAOSHOU) || victim.getType().is(XIUZHE)) {
            int stacks = StatusEffects.has(victim, "effect_034") ? StatusEffects.amplifier(victim, "effect_034") + 1 : 0;
            victim.addEffect(new MobEffectInstance(StatusEffects.holder("effect_034"), 20, stacks, false, false));
        }
        if (attacker instanceof Player player) {
            directPets(player, victim);
        }
        if (victim instanceof Player player) {
            directPets(player, attacker);
        }
    }

    private static void enchantments(LivingEntity victim, LivingEntity attacker, double amount) {
        int haoyu = Enchants.mainHand(attacker, Enchants.key("haoyu"));
        if (haoyu != 0) {
            double factor = 1 - haoyu * 0.01;
            victim.getPersistentData().putDouble("power", victim.getPersistentData().getDouble("power") * factor);
            if (victim instanceof ServerPlayer player) {
                Cultivation.set(player, CultivationValue.POWER, Cultivation.get(player, CultivationValue.POWER) * factor);
            }
        }
        int phoenix = Enchants.mainHand(attacker, Enchants.key("phoenix"));
        if (phoenix != 0 && Math.random() < phoenix * 0.01) {
            victim.setHealth((float) (victim.getHealth() - Math.pow(phoenix, 2)));
        }
        int longDignified = Enchants.mainHand(attacker, Enchants.key("longdignified"));
        if (longDignified != 0 && Math.random() < longDignified * 0.01) {
            victim.addEffect(new MobEffectInstance(StatusEffects.holder("effect_007"), 100, longDignified));
        }
        int tigerDignified = Enchants.mainHand(attacker, Enchants.key("tigerdignified"));
        if (tigerDignified != 0 && Math.random() < tigerDignified * 0.01) {
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, tigerDignified));
        }
        int sawtooth = Enchants.mainHand(attacker, Enchants.key("sawtooth"));
        if (!victim.getType().is(EntityTypeTags.UNDEAD) && sawtooth != 0 && Math.random() < sawtooth * 0.1) {
            victim.addEffect(new MobEffectInstance(StatusEffects.holder("effect_004"), extended(victim, StatusEffects.holder("effect_004")), sawtooth - 1));
        }
        int fluorescence = Enchants.mainHand(attacker, Enchants.key("fluorescence"));
        if (fluorescence != 0 && Math.random() < fluorescence * 0.1) {
            victim.addEffect(new MobEffectInstance(MobEffects.GLOWING, extended(victim, MobEffects.GLOWING), 0));
        }
        for (String[] poison : POISONS) {
            int poisonLevel = Enchants.mainHand(attacker, Enchants.key(poison[0]));
            if (poisonLevel != 0 && Math.random() < poisonLevel * 0.01) {
                victim.addEffect(new MobEffectInstance(StatusEffects.holder(poison[1]), extended(victim, StatusEffects.holder(poison[1])), poisonLevel - 1));
                }
        }
        int vampire = Enchants.mainHand(attacker, Enchants.key("vampire"));
        if (vampire != 0) {
            attacker.setHealth((float) (attacker.getHealth() + amount * vampire * 5 * 0.01));
        }
        if (victim.level() instanceof ServerLevel level) {
            HitEnchantments.run(level, victim, attacker);
        }
    }

    /** 来源的叠加时长：已有同效果的剩余刻数 × 20 + 600（Q32）。 */
    private static int extended(LivingEntity victim, Holder<MobEffect> effect) {
        MobEffectInstance existing = victim.getEffect(effect);
        return (existing == null ? 0 : existing.getDuration()) * 20 + 600;
    }

    /** 以 center 为中心、边长 64 的方体内主人是 center 的已驯服动物，把 target 记为它们的临时攻击目标。 */
    private static void directPets(Player center, Entity target) {
        AABB area = new AABB(center.position(), center.position()).inflate(32);
        for (TamableAnimal pet : center.level().getEntitiesOfClass(TamableAnimal.class, area, TamableAnimal::isTame)) {
            if (pet.getOwner() == center) {
                pet.getPersistentData().putString("linshigongjizhe", target.getStringUUID());
            }
        }
    }
}
