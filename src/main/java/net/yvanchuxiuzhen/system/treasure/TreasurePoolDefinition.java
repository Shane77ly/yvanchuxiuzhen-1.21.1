package net.yvanchuxiuzhen.system.treasure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipeReward;
import net.yvanchuxiuzhen.system.liandan.api.MedicineReward;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;

/**
 * 宝箱、书架、药架、武器架与宝匣的统一奖励池定义。
 * 支持按等级区间筛选分段、加权随机抽样、嵌套子池委派、固定或随机数量区间、强类型物品组件注入与多物品掉落组合。
 */
public record TreasurePoolDefinition(String id, List<DispatchKey> dispatches, List<LevelTier> tiers) {
    public static final Codec<TreasurePoolDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(TreasurePoolDefinition::id),
            DispatchKey.CODEC.listOf().optionalFieldOf("dispatches", List.of()).forGetter(TreasurePoolDefinition::dispatches),
            LevelTier.CODEC.listOf().fieldOf("tiers").forGetter(TreasurePoolDefinition::tiers)
    ).apply(instance, TreasurePoolDefinition::new));

    public TreasurePoolDefinition {
        dispatches = List.copyOf(dispatches);
        tiers = List.copyOf(tiers);
    }

    /** 按定义顺序选择第一个满足宝箱等级的分段；无匹配分段时返回空。 */
    Optional<LevelTier> tierFor(double level) {
        for (LevelTier tier : tiers) {
            if (tier.matches(level)) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    /** 容器（category + subtype）到奖励池 ID 的路由键。 */
    public record DispatchKey(String category, String subtype) {
        static final Codec<DispatchKey> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("category").forGetter(DispatchKey::category),
                Codec.STRING.fieldOf("subtype").forGetter(DispatchKey::subtype)
        ).apply(instance, DispatchKey::new));
    }

    /** 闭区间整数范围，min == max 时表示固定值。 */
    public record IntRange(int min, int max) {
        static final Codec<IntRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("min").forGetter(IntRange::min),
                Codec.INT.fieldOf("max").forGetter(IntRange::max)
        ).apply(instance, IntRange::new));

        int roll(RandomSource random) {
            return min >= max ? min : Mth.nextInt(random, min, max);
        }
    }

    /** 加权品阶分布项。 */
    public record WeightedQuality(int weight, int quality) {
        static final Codec<WeightedQuality> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("weight").forGetter(WeightedQuality::weight),
                Codec.INT.fieldOf("quality").forGetter(WeightedQuality::quality)
        ).apply(instance, WeightedQuality::new));

        static int pick(List<WeightedQuality> qualities, RandomSource random) {
            int total = 0;
            for (WeightedQuality entry : qualities) {
                if (entry.weight() > 0) {
                    total += entry.weight();
                }
            }
            if (total <= 0) {
                return 0;
            }
            int roll = Mth.nextInt(random, 0, total - 1);
            for (WeightedQuality entry : qualities) {
                if (entry.weight() <= 0) {
                    continue;
                }
                roll -= entry.weight();
                if (roll < 0) {
                    return entry.quality();
                }
            }
            return qualities.getLast().quality();
        }
    }

    /** 同一等级分段内条目共享的材料灵气/品阶、丹药装瓶数量与符箓等阶上下文。 */
    public record TierContext(Optional<IntRange> materialLevel,
                       Optional<List<WeightedQuality>> materialQualities,
                       Optional<IntRange> medicinePackCount,
                       Optional<Integer> fuluLevel,
                       Optional<String> fuluTierText) {
        public static final TierContext EMPTY = new TierContext(
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        static final Codec<TierContext> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                IntRange.CODEC.optionalFieldOf("material_level").forGetter(TierContext::materialLevel),
                WeightedQuality.CODEC.listOf().optionalFieldOf("material_qualities").forGetter(TierContext::materialQualities),
                IntRange.CODEC.optionalFieldOf("medicine_pack_count").forGetter(TierContext::medicinePackCount),
                Codec.INT.optionalFieldOf("fulu_level").forGetter(TierContext::fuluLevel),
                Codec.STRING.optionalFieldOf("fulu_tier_text").forGetter(TierContext::fuluTierText)
        ).apply(instance, TierContext::new));

        public TierContext {
            materialQualities = materialQualities.map(List::copyOf);
        }
    }

    /** 按宝箱等级划分的分段与候选条目列表。 */
    public record LevelTier(Optional<Double> minLevel,
                     Optional<Double> maxLevel,
                     Optional<TierContext> context,
                     List<WeightedEntry> entries) {
        static final Codec<LevelTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("min_level").forGetter(LevelTier::minLevel),
                Codec.DOUBLE.optionalFieldOf("max_level").forGetter(LevelTier::maxLevel),
                TierContext.CODEC.optionalFieldOf("context").forGetter(LevelTier::context),
                WeightedEntry.CODEC.listOf().fieldOf("entries").forGetter(LevelTier::entries)
        ).apply(instance, LevelTier::new));

        public LevelTier {
            entries = List.copyOf(entries);
        }

        boolean matches(double level) {
            if (minLevel.isPresent() && level < minLevel.get()) {
                return false;
            }
            if (maxLevel.isPresent() && level > maxLevel.get()) {
                return false;
            }
            return true;
        }

        TierContext effectiveContext() {
            return context.orElse(TierContext.EMPTY);
        }

        Optional<WeightedEntry> pick(RandomSource random) {
            int total = 0;
            for (WeightedEntry entry : entries) {
                if (entry.weight() > 0) {
                    total += entry.weight();
                }
            }
            if (total <= 0) {
                return Optional.empty();
            }
            int roll = Mth.nextInt(random, 0, total - 1);
            for (WeightedEntry entry : entries) {
                if (entry.weight() <= 0) {
                    continue;
                }
                roll -= entry.weight();
                if (roll < 0) {
                    return Optional.of(entry);
                }
            }
            return Optional.of(entries.getLast());
        }
    }

    /** 加权奖励条目，权重默认为 1。 */
    public record WeightedEntry(int weight, Reward reward) {
        static final Codec<WeightedEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedEntry::weight),
                Reward.CODEC.fieldOf("reward").forGetter(WeightedEntry::reward)
        ).apply(instance, WeightedEntry::new));
    }

    /** 强类型奖励规约，按 {@code kind} 字段多态反序列化，彻底替代旧版 {@code %}/{@code /} 拼接串。 */
    public sealed interface Reward permits EmptyReward, PoolRefReward, CommandReward, ItemReward, MaterialReward,
            FuluReward, MedicineRewardSpec, RecipeRewardSpec, TechniqueReward, SkillReward, MultiDropReward {
        Codec<Reward> CODEC = Codec.recursive("TreasureRewardSpec", self -> Codec.STRING.dispatch("kind", Reward::kind, kind -> switch (kind) {
            case "empty" -> EmptyReward.MAP_CODEC;
            case "pool" -> PoolRefReward.MAP_CODEC;
            case "command" -> CommandReward.MAP_CODEC;
            case "item" -> ItemReward.MAP_CODEC;
            case "material" -> MaterialReward.MAP_CODEC;
            case "fulu" -> FuluReward.MAP_CODEC;
            case "medicine" -> MedicineRewardSpec.MAP_CODEC;
            case "recipe" -> RecipeRewardSpec.MAP_CODEC;
            case "technique" -> TechniqueReward.MAP_CODEC;
            case "skill" -> SkillReward.MAP_CODEC;
            case "multi" -> MultiDropReward.codec(self);
            default -> throw new IllegalArgumentException("未知奖励类型：" + kind);
        }));

        String kind();
    }

    /** 空奖励：沿用来源总表空分支语义，中止本次开箱后续发放。 */
    public record EmptyReward() implements Reward {
        static final EmptyReward INSTANCE = new EmptyReward();
        static final MapCodec<EmptyReward> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public String kind() {
            return "empty";
        }
    }

    /** 委派抽取另一个奖励池（可选指定抽取次数区间，默认 1 次）。 */
    public record PoolRefReward(String pool, Optional<IntRange> rolls) implements Reward {
        static final MapCodec<PoolRefReward> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("pool").forGetter(PoolRefReward::pool),
                IntRange.CODEC.optionalFieldOf("rolls").forGetter(PoolRefReward::rolls)
        ).apply(instance, PoolRefReward::new));

        @Override
        public String kind() {
            return "pool";
        }
    }

    /** 保留供阶段 3 重构的炼器/饰品命令奖励项。 */
    public record CommandReward(String command) implements Reward {
        static final MapCodec<CommandReward> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("command").forGetter(CommandReward::command)
        ).apply(instance, CommandReward::new));

        @Override
        public String kind() {
            return "command";
        }
    }

    /** 普通物品奖励，可选堆叠数量区间与自定义显示名称。 */
    public record ItemReward(ResourceLocation item, Optional<IntRange> count, Optional<String> displayName) implements Reward {
        static final MapCodec<ItemReward> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(ItemReward::item),
                IntRange.CODEC.optionalFieldOf("count").forGetter(ItemReward::count),
                Codec.STRING.optionalFieldOf("display_name").forGetter(ItemReward::displayName)
        ).apply(instance, ItemReward::new));

        @Override
        public String kind() {
            return "item";
        }
    }

    /** 带灵气等级与品阶加权分布的材料实例奖励。 */
    public record MaterialReward(ResourceLocation item,
                          Optional<IntRange> materialLevel,
                          Optional<List<WeightedQuality>> materialQualities,
                          Optional<IntRange> count) implements Reward {
        static final MapCodec<MaterialReward> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(MaterialReward::item),
                IntRange.CODEC.optionalFieldOf("material_level").forGetter(MaterialReward::materialLevel),
                WeightedQuality.CODEC.listOf().optionalFieldOf("material_qualities").forGetter(MaterialReward::materialQualities),
                IntRange.CODEC.optionalFieldOf("count").forGetter(MaterialReward::count)
        ).apply(instance, MaterialReward::new));

        public MaterialReward {
            materialQualities = materialQualities.map(List::copyOf);
        }

        @Override
        public String kind() {
            return "material";
        }
    }

    /** 符箓奖励：注入符箓等级与品阶文字（缺省时取分段上下文）。 */
    public record FuluReward(ResourceLocation item,
                      Optional<Integer> fuluLevel,
                      Optional<String> fuluTierText,
                      Optional<IntRange> count) implements Reward {
        static final MapCodec<FuluReward> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(FuluReward::item),
                Codec.INT.optionalFieldOf("fulu_level").forGetter(FuluReward::fuluLevel),
                Codec.STRING.optionalFieldOf("fulu_tier_text").forGetter(FuluReward::fuluTierText),
                IntRange.CODEC.optionalFieldOf("count").forGetter(FuluReward::count)
        ).apply(instance, FuluReward::new));

        @Override
        public String kind() {
            return "fulu";
        }
    }

    /** 宝箱丹药奖励：直接携带药效列表与随机装瓶数区间。 */
    public record MedicineRewardSpec(ResourceLocation item,
                              String name,
                              int dose,
                              Optional<IntRange> packCount,
                              boolean treasure,
                              List<MedicineReward.Effect> effects,
                              Optional<IntRange> count) implements Reward {
        private static final Codec<MedicineReward.Effect> EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(MedicineReward.Effect::id),
                Codec.INT.fieldOf("level").forGetter(MedicineReward.Effect::level),
                Codec.INT.fieldOf("duration").forGetter(MedicineReward.Effect::duration)
        ).apply(instance, MedicineReward.Effect::new));

        static final MapCodec<MedicineRewardSpec> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(MedicineRewardSpec::item),
                Codec.STRING.fieldOf("name").forGetter(MedicineRewardSpec::name),
                Codec.INT.fieldOf("dose").forGetter(MedicineRewardSpec::dose),
                IntRange.CODEC.optionalFieldOf("pack_count").forGetter(MedicineRewardSpec::packCount),
                Codec.BOOL.optionalFieldOf("treasure", true).forGetter(MedicineRewardSpec::treasure),
                EFFECT_CODEC.listOf().fieldOf("effects").forGetter(MedicineRewardSpec::effects),
                IntRange.CODEC.optionalFieldOf("count").forGetter(MedicineRewardSpec::count)
        ).apply(instance, MedicineRewardSpec::new));

        public MedicineRewardSpec {
            effects = List.copyOf(effects);
        }

        @Override
        public String kind() {
            return "medicine";
        }
    }

    /** 丹方奖励：直接映射丹方定义与有序材料列表。 */
    public record RecipeRewardSpec(ResourceLocation item,
                            String medicineName,
                            String recipeDescription,
                            String medicineDescription,
                            String levelText,
                            int level,
                            List<MedicineRecipeReward.Ingredient> ingredients,
                            Optional<IntRange> count) implements Reward {
        private static final Codec<MedicineRecipeReward.Ingredient> INGREDIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(MedicineRecipeReward.Ingredient::id),
                Codec.STRING.fieldOf("name").forGetter(MedicineRecipeReward.Ingredient::name)
        ).apply(instance, MedicineRecipeReward.Ingredient::new));

        static final MapCodec<RecipeRewardSpec> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(RecipeRewardSpec::item),
                Codec.STRING.fieldOf("medicine_name").forGetter(RecipeRewardSpec::medicineName),
                Codec.STRING.fieldOf("recipe_description").forGetter(RecipeRewardSpec::recipeDescription),
                Codec.STRING.fieldOf("medicine_description").forGetter(RecipeRewardSpec::medicineDescription),
                Codec.STRING.fieldOf("level_text").forGetter(RecipeRewardSpec::levelText),
                Codec.INT.fieldOf("level").forGetter(RecipeRewardSpec::level),
                INGREDIENT_CODEC.listOf().fieldOf("ingredients").forGetter(RecipeRewardSpec::ingredients),
                IntRange.CODEC.optionalFieldOf("count").forGetter(RecipeRewardSpec::count)
        ).apply(instance, RecipeRewardSpec::new));

        public RecipeRewardSpec {
            ingredients = List.copyOf(ingredients);
        }

        @Override
        public String kind() {
            return "recipe";
        }
    }

    /** 功法书奖励，直接复用功法系统的 {@link TechniqueDefinition#MAP_CODEC}。 */
    public record TechniqueReward(TechniqueDefinition definition, Optional<IntRange> count) implements Reward {
        static final MapCodec<TechniqueReward> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TechniqueDefinition.MAP_CODEC.forGetter(TechniqueReward::definition),
                IntRange.CODEC.optionalFieldOf("count").forGetter(TechniqueReward::count)
        ).apply(instance, TechniqueReward::new));

        @Override
        public String kind() {
            return "technique";
        }
    }

    /** 技能、身法与轻功书奖励。 */
    public record SkillReward(ResourceLocation item,
                       SkillKind skillKind,
                       String name,
                       double grade,
                       double level,
                       String body,
                       String intro,
                       Map<String, Double> numbers,
                       Map<String, String> texts,
                       Optional<IntRange> count) implements Reward {
        static final MapCodec<SkillReward> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(SkillReward::item),
                SkillKind.CODEC.fieldOf("skill_kind").forGetter(SkillReward::skillKind),
                Codec.STRING.fieldOf("name").forGetter(SkillReward::name),
                Codec.DOUBLE.fieldOf("grade").forGetter(SkillReward::grade),
                Codec.DOUBLE.fieldOf("level").forGetter(SkillReward::level),
                Codec.STRING.fieldOf("body").forGetter(SkillReward::body),
                Codec.STRING.fieldOf("intro").forGetter(SkillReward::intro),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("numbers", Map.of()).forGetter(SkillReward::numbers),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("texts", Map.of()).forGetter(SkillReward::texts),
                IntRange.CODEC.optionalFieldOf("count").forGetter(SkillReward::count)
        ).apply(instance, SkillReward::new));

        public SkillReward {
            numbers = Map.copyOf(numbers);
            texts = Map.copyOf(texts);
        }

        @Override
        public String kind() {
            return "skill";
        }
    }

    /** 多物品掉落组合：单次抽取可同时产出多项奖励。 */
    public record MultiDropReward(List<Reward> rewards) implements Reward {
        static MapCodec<MultiDropReward> codec(Codec<Reward> self) {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                    self.listOf().fieldOf("rewards").forGetter(MultiDropReward::rewards)
            ).apply(instance, MultiDropReward::new));
        }

        public MultiDropReward {
            rewards = List.copyOf(rewards);
        }

        @Override
        public String kind() {
            return "multi";
        }
    }
}
