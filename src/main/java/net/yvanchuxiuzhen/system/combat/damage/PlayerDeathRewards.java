package net.yvanchuxiuzhen.system.combat.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;

/**
 * 玩家击杀收益入口，对应 1.18.2 版本 {@code EntityDeathProcedure:37–144,291–299}。
 * 只从 {@link LivingDeathEvent} 的 {@link net.minecraft.world.damagesource.DamageSource} 读取攻击者，
 * 因而普通攻击和技能经 {@code CombatDamageTypes.source} 传入的玩家归属使用同一条结算路径。
 */
public final class PlayerDeathRewards {
    private static final String LEVEL_NUMBER = "LEVELnumber";
    private static final String ADDITIONAL_EXPERIENCE = "additional_experience";
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));
    private static final ResourceKey<Level> HUMAN_REALM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionrenjian"));

    private PlayerDeathRewards() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(PlayerDeathRewards::onDeath);
    }

    private static void onDeath(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer killer)) {
            return;
        }
        LivingEntity victim = event.getEntity();
        double victimLevel = victim.getPersistentData().getDouble(LEVEL_NUMBER);
        double experience = experience(victim, victimLevel);
        experience *= 1 + techniqueBonus(killer);
        Cultivation.add(killer, CultivationValue.EXPERIENCE_STAND, experience);
        if (victimLevel >= Cultivation.get(killer, CultivationValue.LEVEL_NUMBER)) {
            Cultivation.add(killer, CultivationValue.VIGOROUS,
                    -Math.pow(victimLevel - Cultivation.get(killer, CultivationValue.LEVEL_NUMBER) + 1, 2));
        }
        awardMerits(killer, victim, victimLevel);
        if (victim instanceof ServerPlayer player && player.level().dimension().equals(HUMAN_REALM)) {
            Cultivation.set(player, CultivationText.REVIVE, "人间界");
        }
    }

    private static double experience(LivingEntity victim, double victimLevel) {
        if (victimLevel >= 1) {
            return Math.pow(victimLevel, 2.4) + victimLevel * 4 + 5
                    + victim.getPersistentData().getDouble(ADDITIONAL_EXPERIENCE);
        }
        if (victim instanceof Mob || victim instanceof Animal) {
            return victim.getMaxHealth() / 5;
        }
        return 0;
    }

    private static double techniqueBonus(ServerPlayer killer) {
        MainTechnique technique = Cultivation.mainTechnique(killer);
        if (technique.name().equals("混元五行功")) {
            int activeRoots = 0;
            activeRoots += Cultivation.get(killer, CultivationValue.ROOT_GOLD) >= 1 ? 1 : 0;
            activeRoots += Cultivation.get(killer, CultivationValue.ROOT_WATER) >= 1 ? 1 : 0;
            activeRoots += Cultivation.get(killer, CultivationValue.ROOT_TREE) >= 1 ? 1 : 0;
            activeRoots += Cultivation.get(killer, CultivationValue.ROOT_FIRE) >= 1 ? 1 : 0;
            activeRoots += Cultivation.get(killer, CultivationValue.ROOT_SOIL) >= 1 ? 1 : 0;
            return activeRoots * 0.1;
        }
        if (technique.name().equals("五行补天神功")) {
            double roots = Cultivation.get(killer, CultivationValue.ROOT_GOLD)
                    + Cultivation.get(killer, CultivationValue.ROOT_WATER)
                    + Cultivation.get(killer, CultivationValue.ROOT_TREE)
                    + Cultivation.get(killer, CultivationValue.ROOT_FIRE)
                    + Cultivation.get(killer, CultivationValue.ROOT_SOIL);
            return roots * 0.05;
        }
        return 0;
    }

    private static void awardMerits(ServerPlayer killer, LivingEntity victim, double victimLevel) {
        if (!victim.getType().is(YAOSHOU) && victim instanceof Animal) {
            Cultivation.add(killer, CultivationValue.MERITS, -1);
        } else if (victim.getMobType() == MobType.UNDEAD) {
            Cultivation.add(killer, CultivationValue.MERITS, victimLevel + 2);
        } else if (victim instanceof Mob) {
            Cultivation.add(killer, CultivationValue.MERITS, victimLevel + 1);
        }
    }
}