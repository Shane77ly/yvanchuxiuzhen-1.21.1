package net.yvanchuxiuzhen.system.cooking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 烹饪与酿酒数据表项，通过 {@code kind} 派发强类型 {@link Codec}。 */
public sealed interface CookingRuleDefinition
        permits CookingRuleDefinition.StillBrewingRule,
        CookingRuleDefinition.WineDrinkRule,
        CookingRuleDefinition.InkBlankRule,
        CookingRuleDefinition.MealPlateRule,
        CookingRuleDefinition.PotContainerRule {

    Codec<CookingRuleDefinition> CODEC = Codec.STRING.dispatch("kind", CookingRuleDefinition::kind, CookingRuleDefinition::codecFor);

    String kind();

    private static MapCodec<? extends CookingRuleDefinition> codecFor(String kind) {
        return switch (kind) {
            case "still_brewing" -> StillBrewingRule.MAP_CODEC;
            case "wine_drink" -> WineDrinkRule.MAP_CODEC;
            case "ink_blank" -> InkBlankRule.MAP_CODEC;
            case "meal_plate" -> MealPlateRule.MAP_CODEC;
            case "pot_container" -> PotContainerRule.MAP_CODEC;
            default -> throw new IllegalArgumentException("未知烹饪配方类型: " + kind);
        };
    }

    /** 木甑酿酒配方。 */
    record StillBrewingRule(String outputId, int brewTicks, Map<String, Integer> ingredients) implements CookingRuleDefinition {
        public static final MapCodec<StillBrewingRule> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("output_id").forGetter(StillBrewingRule::outputId),
                Codec.INT.fieldOf("brew_ticks").forGetter(StillBrewingRule::brewTicks),
                Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("ingredients").forGetter(StillBrewingRule::ingredients)
        ).apply(instance, StillBrewingRule::new));

        @Override
        public String kind() {
            return "still_brewing";
        }
    }

    /** 药水效果项；未指定固定等级时按年份阶梯放大。 */
    record WineEffectEntry(String effectId, double durationMultiplier, Optional<Integer> fixedAmplifier) {
        public static final Codec<WineEffectEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("effect_id").forGetter(WineEffectEntry::effectId),
                Codec.DOUBLE.fieldOf("duration_multiplier").forGetter(WineEffectEntry::durationMultiplier),
                Codec.INT.optionalFieldOf("fixed_amplifier").forGetter(WineEffectEntry::fixedAmplifier)
        ).apply(instance, WineEffectEntry::new));
    }

    /** 饮酒结算规则。 */
    record WineDrinkRule(
            String wineId,
            double alcohol,
            double experience,
            double bloodExperience,
            Optional<String> bloodRemovesEffect,
            List<WineEffectEntry> bloodEffects,
            List<WineEffectEntry> effects) implements CookingRuleDefinition {
        public static final MapCodec<WineDrinkRule> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("wine_id").forGetter(WineDrinkRule::wineId),
                Codec.DOUBLE.fieldOf("alcohol").forGetter(WineDrinkRule::alcohol),
                Codec.DOUBLE.fieldOf("experience").forGetter(WineDrinkRule::experience),
                Codec.DOUBLE.fieldOf("blood_experience").forGetter(WineDrinkRule::bloodExperience),
                Codec.STRING.optionalFieldOf("blood_removes_effect").forGetter(WineDrinkRule::bloodRemovesEffect),
                WineEffectEntry.CODEC.listOf().fieldOf("blood_effects").forGetter(WineDrinkRule::bloodEffects),
                WineEffectEntry.CODEC.listOf().fieldOf("effects").forGetter(WineDrinkRule::effects)
        ).apply(instance, WineDrinkRule::new));

        @Override
        public String kind() {
            return "wine_drink";
        }
    }

    /** 墨坯妖兽材料类别分组。 */
    record InkCategoryGroup(int category, List<String> tags) {
        public static final Codec<InkCategoryGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("category").forGetter(InkCategoryGroup::category),
                Codec.STRING.listOf().fieldOf("tags").forGetter(InkCategoryGroup::tags)
        ).apply(instance, InkCategoryGroup::new));
    }

    /** 物品数量条目。 */
    record ItemRequirement(String itemId, int count) {
        public static final Codec<ItemRequirement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item_id").forGetter(ItemRequirement::itemId),
                Codec.INT.fieldOf("count").forGetter(ItemRequirement::count)
        ).apply(instance, ItemRequirement::new));
    }

    /** 墨坯平均等级分档结果。 */
    record InkGradeTier(double minAverage, String outputId) {
        public static final Codec<InkGradeTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("min_average").forGetter(InkGradeTier::minAverage),
                Codec.STRING.fieldOf("output_id").forGetter(InkGradeTier::outputId)
        ).apply(instance, InkGradeTier::new));
    }

    /** 无菜谱墨料坯熬制规则。 */
    record InkBlankRule(
            int delayTicks,
            String materialTag,
            List<InkCategoryGroup> categoryGroups,
            List<ItemRequirement> reagents,
            List<InkGradeTier> gradeTiers) implements CookingRuleDefinition {
        public static final MapCodec<InkBlankRule> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("delay_ticks").forGetter(InkBlankRule::delayTicks),
                Codec.STRING.fieldOf("material_tag").forGetter(InkBlankRule::materialTag),
                InkCategoryGroup.CODEC.listOf().fieldOf("category_groups").forGetter(InkBlankRule::categoryGroups),
                ItemRequirement.CODEC.listOf().fieldOf("reagents").forGetter(InkBlankRule::reagents),
                InkGradeTier.CODEC.listOf().fieldOf("grade_tiers").forGetter(InkBlankRule::gradeTiers)
        ).apply(instance, InkBlankRule::new));

        @Override
        public String kind() {
            return "ink_blank";
        }
    }

    /** 摆盘食物分类与容器还原规则。 */
    record MealPlateRule(
            int category,
            String itemTag,
            String blockTag,
            String containerBlock,
            int nutrition,
            int saturation,
            Optional<String> markerEffect) implements CookingRuleDefinition {
        public static final MapCodec<MealPlateRule> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("category").forGetter(MealPlateRule::category),
                Codec.STRING.fieldOf("item_tag").forGetter(MealPlateRule::itemTag),
                Codec.STRING.fieldOf("block_tag").forGetter(MealPlateRule::blockTag),
                Codec.STRING.fieldOf("container_block").forGetter(MealPlateRule::containerBlock),
                Codec.INT.fieldOf("nutrition").forGetter(MealPlateRule::nutrition),
                Codec.INT.fieldOf("saturation").forGetter(MealPlateRule::saturation),
                Codec.STRING.optionalFieldOf("marker_effect").forGetter(MealPlateRule::markerEffect)
        ).apply(instance, MealPlateRule::new));

        @Override
        public String kind() {
            return "meal_plate";
        }
    }

    /** 烹饪锅具所需盛装容器与缺容器提示映射。 */
    record PotContainerRule(int slotOffset, String itemId, String potError, String steamerErrorKey)
            implements CookingRuleDefinition {
        public static final MapCodec<PotContainerRule> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("slot_offset").forGetter(PotContainerRule::slotOffset),
                Codec.STRING.fieldOf("item_id").forGetter(PotContainerRule::itemId),
                Codec.STRING.fieldOf("pot_error").forGetter(PotContainerRule::potError),
                Codec.STRING.fieldOf("steamer_error_key").forGetter(PotContainerRule::steamerErrorKey)
        ).apply(instance, PotContainerRule::new));

        @Override
        public String kind() {
            return "pot_container";
        }
    }
}
