package net.yvanchuxiuzhen.system.combat.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 死亡事件中与玩家有关的结算：击杀收益与人间道重生标记。 */
public final class PlayerDeathRules {
    private static final ResourceKey<Level> HUMAN_REALM = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionrenjian"));
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));
    private static final CultivationValue[] ROOTS = {CultivationValue.ROOT_GOLD, CultivationValue.ROOT_WATER,
            CultivationValue.ROOT_TREE, CultivationValue.ROOT_FIRE, CultivationValue.ROOT_SOIL};

    private PlayerDeathRules() {
    }

    public static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) {
            return;
        }
        ServerPlayer killer = CombatAttribution.player(event.getSource());
        if (killer != null) {
            reward(killer, victim);
        }
        if (victim instanceof ServerPlayer player && player.level().dimension().equals(HUMAN_REALM)) {
            Cultivation.set(player, CultivationText.REVIVE, "人间界");
        }
    }

    private static void reward(ServerPlayer killer, LivingEntity victim) {
        CompoundTag data = victim.getPersistentData();
        double level = victimLevel(victim, data);
        double experience = baseExperience(victim, data, level);
        Cultivation.add(killer, CultivationValue.EXPERIENCE_STAND, experience + experienceBonus(killer, experience));

        double killerLevel = Cultivation.get(killer, CultivationValue.LEVEL_NUMBER);
        if (level >= killerLevel) {
            double penalty = Math.pow(level - killerLevel + 1, 2);
            if (penalty > 0) {
                Cultivation.add(killer, CultivationValue.VIGOROUS, -penalty);
            }
        }
        if (victim instanceof Animal && !victim.getType().is(YAOSHOU)) {
            Cultivation.add(killer, CultivationValue.MERITS, -1);
        } else if (victim.getType().is(EntityTypeTags.UNDEAD)) {
            Cultivation.add(killer, CultivationValue.MERITS, level + 2);
        } else if (victim instanceof Mob) {
            Cultivation.add(killer, CultivationValue.MERITS, level + 1);
        }
    }

    /** 玩家境界以修炼附件为真值，读取旧字段消费者前先刷新 LEVELnumber 镜像。 */
    private static double victimLevel(LivingEntity victim, CompoundTag data) {
        if (victim instanceof ServerPlayer player) {
            Cultivation.syncLegacyLevelMirror(player);
            return Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
        }
        return data.getDouble("LEVELnumber");
    }

    /**
     * 来源按实体等级优先；无等级时，Mob（包括 Animal）按实际生命上限的五分之一给修为。
     * 不属于 Mob 的实体没有默认击杀修为。
     */
    private static double baseExperience(LivingEntity victim, CompoundTag data, double level) {
        if (level >= 1) {
            return Math.pow(level, 2.4) + level * 4 + 5 + data.getDouble("additional_experience");
        }
        if (victim instanceof Mob) {
            return victim.getMaxHealth() / 5.0F;
        }
        return 0;
    }

    private static double experienceBonus(ServerPlayer killer, double experience) {
        String name = Cultivation.mainTechnique(killer).name();
        if (name.equals("混元五行功")) {
            int percent = 0;
            for (CultivationValue root : ROOTS) {
                if (Cultivation.get(killer, root) >= 1) {
                    percent += 10;
                }
            }
            return experience * percent * 0.01;
        }
        if (name.equals("五行补天神功")) {
            double sum = 0;
            for (CultivationValue root : ROOTS) {
                sum += Cultivation.get(killer, root);
            }
            return experience * (sum * 0.05);
        }
        return 0;
    }
}
