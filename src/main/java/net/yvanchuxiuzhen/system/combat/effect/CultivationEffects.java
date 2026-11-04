package net.yvanchuxiuzhen.system.combat.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.EffectExperienceEvent;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 修炼类状态效果：永久加点机会、灵力与精力恢复、提高修为。数值写入走 xiulian 的公开操作；
 * 来源的过程对非玩家实体读写的是 PlayerVariables 的默认值，等于没有作用，这里只处理玩家（妖兽的修为经事件交给 creature）。
 * 瞬时类效果在第一刻生效后立即移除。
 */
final class CultivationEffects {
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private CultivationEffects() {
    }

    static void register(ContentTables tables) {
        tables.effectBehavior("effect_008", new SkillChance("effect_008", CultivationValue.SKILL_POINTS_HEALTH_MAX, "血量上限"));
        tables.effectBehavior("effect_009", new SkillChance("effect_009", CultivationValue.SKILL_POINTS_ATTACK, "物理攻击上限"));
        tables.effectBehavior("effect_012", new SkillChance("effect_012", CultivationValue.SKILL_POINTS_POWER_MAX, "灵力上限"));
        tables.effectBehavior("effect_014", new SkillChance("effect_014", CultivationValue.SKILL_POINTS_MANA, "法术攻击"));
        tables.effectBehavior("effect_019", new SkillChance("effect_019", CultivationValue.SKILL_POINTS_SOUL_MAX, "精力上限"));
        tables.effectBehavior("effect_021", new SkillChance("effect_021", CultivationValue.SKILL_POINTS_SOUL, "精神攻击"));
        tables.effectBehavior("effect_010", new Restore("effect_010", "effect010", 20, CultivationValue.POWER, Stat.POWER_MAX, false));
        tables.effectBehavior("effect_011", new Restore("effect_011", null, 0, CultivationValue.POWER, Stat.POWER_MAX, true));
        tables.effectBehavior("effect_017", new Restore("effect_017", "effect017", 20, CultivationValue.SOUL, Stat.SOUL_MAX, false));
        tables.effectBehavior("effect_018", new Restore("effect_018", null, 0, CultivationValue.SOUL, Stat.SOUL_MAX, true));
        tables.effectBehavior("effect_015", new Experience("effect_015", "effect015", 40, 5, false));
        tables.effectBehavior("effect_016", new Experience("effect_016", null, 0, 20, true));
    }

    /** 永久加点机会：把对应的已加点次数减 (等级 + 1)，使可再加点的次数增加，提示后移除效果。 */
    private record SkillChance(String effect, CultivationValue counter, String label) implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (entity instanceof ServerPlayer player) {
                Cultivation.add(player, counter, -(amplifier + 1));
                player.displayClientMessage(Component.literal("可多为" + label + "升级" + (amplifier + 1) + "次"), false);
            }
            EffectSupport.remove(entity, effect);
            return true;
        }
    }

    /**
     * 灵力或精力恢复。持续型每 {@code period} 刻恢复 10 × (等级 + 1)，瞬时型一次恢复 (等级 + 1)² × 100 并移除效果；
     * 当前值低于最终上限时才恢复，恢复量不按上限截断。
     */
    private record Restore(String effect, String counterKey, int period, CultivationValue value, Stat limit, boolean instant) implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (instant) {
                restore(entity, Math.pow(amplifier + 1, 2) * 100);
                EffectSupport.remove(entity, effect);
            } else if (EffectSupport.every(entity, counterKey, period)) {
                restore(entity, (amplifier + 1) * 10);
            }
            return true;
        }

        private void restore(LivingEntity entity, double amount) {
            if (entity instanceof ServerPlayer player && Cultivation.get(player, value) < PlayerAttributes.value(player, limit)) {
                Cultivation.add(player, value, amount);
            }
        }
    }

    /** 提高修为：玩家增加待吸收修为，妖兽发出事件；持续型每 40 刻增加 ((等级 + 1) × 5)²，瞬时型一次增加 ((等级 + 1) × 20)² 并提示。 */
    private record Experience(String effect, String counterKey, int period, int factor, boolean instant) implements EffectBehavior {
        @Override
        public boolean tick(LivingEntity entity, int amplifier) {
            if (instant) {
                grant(entity, Math.pow((amplifier + 1) * factor, 2), true);
                EffectSupport.remove(entity, effect);
            } else if (EffectSupport.every(entity, counterKey, period)) {
                grant(entity, Math.pow((amplifier + 1) * factor, 2), false);
            }
            return true;
        }

        private static void grant(LivingEntity entity, double amount, boolean announce) {
            if (entity instanceof ServerPlayer player) {
                Cultivation.add(player, CultivationValue.EXPERIENCE_STAND, amount);
                if (announce) {
                    player.displayClientMessage(Component.literal("修为增加" + amount + "点"), false);
                }
            } else if (entity.getType().is(YAOSHOU)) {
                NeoForge.EVENT_BUS.post(new EffectExperienceEvent(entity, amount));
            }
        }
    }
}
