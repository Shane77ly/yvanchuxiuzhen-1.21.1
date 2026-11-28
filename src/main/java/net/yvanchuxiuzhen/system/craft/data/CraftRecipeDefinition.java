package net.yvanchuxiuzhen.system.craft.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

/** 生活工艺配方定义，通过 {@code kind} 派发强类型 {@link Codec}。 */
public sealed interface CraftRecipeDefinition
        permits CraftRecipeDefinition.CarpentryRecipe,
        CraftRecipeDefinition.GrindingRecipe,
        CraftRecipeDefinition.SilkReelRecipe,
        CraftRecipeDefinition.LoomBasicRecipe,
        CraftRecipeDefinition.LoomPairRecipe,
        CraftRecipeDefinition.LoomCloudRecipe,
        CraftRecipeDefinition.PotteryRecipe {

    Codec<CraftRecipeDefinition> CODEC = Codec.STRING.dispatch("kind", CraftRecipeDefinition::kind, CraftRecipeDefinition::codecFor);

    String kind();

    private static MapCodec<? extends CraftRecipeDefinition> codecFor(String kind) {
        return switch (kind) {
            case "carpentry" -> CarpentryRecipe.MAP_CODEC;
            case "grinding" -> GrindingRecipe.MAP_CODEC;
            case "silk_reel" -> SilkReelRecipe.MAP_CODEC;
            case "loom_basic" -> LoomBasicRecipe.MAP_CODEC;
            case "loom_pair" -> LoomPairRecipe.MAP_CODEC;
            case "loom_cloud" -> LoomCloudRecipe.MAP_CODEC;
            case "pottery" -> PotteryRecipe.MAP_CODEC;
            default -> throw new IllegalArgumentException("未知生活工艺配方类型: " + kind);
        };
    }

    /** 物品耗材条目。 */
    record ItemCost(String itemId, int count) {
        public static final Codec<ItemCost> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("item_id").forGetter(ItemCost::itemId),
                Codec.INT.fieldOf("count").forGetter(ItemCost::count)
        ).apply(instance, ItemCost::new));
    }

    /** 木匠台配方。 */
    record CarpentryRecipe(String category, String outputId, List<ItemCost> costs) implements CraftRecipeDefinition {
        public static final MapCodec<CarpentryRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("category").forGetter(CarpentryRecipe::category),
                Codec.STRING.fieldOf("output_id").forGetter(CarpentryRecipe::outputId),
                ItemCost.CODEC.listOf().fieldOf("costs").forGetter(CarpentryRecipe::costs)
        ).apply(instance, CarpentryRecipe::new));

        @Override
        public String kind() {
            return "carpentry";
        }
    }

    /** 石磨研磨配方。 */
    record GrindingRecipe(String inputId, int cost, String outputId, Optional<String> auxiliaryId) implements CraftRecipeDefinition {
        public static final MapCodec<GrindingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("input_id").forGetter(GrindingRecipe::inputId),
                Codec.INT.fieldOf("cost").forGetter(GrindingRecipe::cost),
                Codec.STRING.fieldOf("output_id").forGetter(GrindingRecipe::outputId),
                Codec.STRING.optionalFieldOf("auxiliary_id").forGetter(GrindingRecipe::auxiliaryId)
        ).apply(instance, GrindingRecipe::new));

        @Override
        public String kind() {
            return "grinding";
        }
    }

    /** 缫丝轮产丝配方。 */
    record SilkReelRecipe(String inputId, String outputId) implements CraftRecipeDefinition {
        public static final MapCodec<SilkReelRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("input_id").forGetter(SilkReelRecipe::inputId),
                Codec.STRING.fieldOf("output_id").forGetter(SilkReelRecipe::outputId)
        ).apply(instance, SilkReelRecipe::new));

        @Override
        public String kind() {
            return "silk_reel";
        }
    }

    /** 织布机基础纺织配方。 */
    record LoomBasicRecipe(String inputId, String outputId, int materialLevel) implements CraftRecipeDefinition {
        public static final MapCodec<LoomBasicRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("input_id").forGetter(LoomBasicRecipe::inputId),
                Codec.STRING.fieldOf("output_id").forGetter(LoomBasicRecipe::outputId),
                Codec.INT.fieldOf("material_level").forGetter(LoomBasicRecipe::materialLevel)
        ).apply(instance, LoomBasicRecipe::new));

        @Override
        public String kind() {
            return "loom_basic";
        }
    }

    /** 织布机双主副材料交织配方（对称支持任一主手搭配另一背包辅料）。 */
    record LoomPairRecipe(String inputA, String inputB, String outputId, int count, int materialLevel)
            implements CraftRecipeDefinition {
        public static final MapCodec<LoomPairRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("input_a").forGetter(LoomPairRecipe::inputA),
                Codec.STRING.fieldOf("input_b").forGetter(LoomPairRecipe::inputB),
                Codec.STRING.fieldOf("output_id").forGetter(LoomPairRecipe::outputId),
                Codec.INT.fieldOf("count").forGetter(LoomPairRecipe::count),
                Codec.INT.fieldOf("material_level").forGetter(LoomPairRecipe::materialLevel)
        ).apply(instance, LoomPairRecipe::new));

        @Override
        public String kind() {
            return "loom_pair";
        }
    }

    /** 织布机流云布合成配方（按配置顺序匹配背包布料）。 */
    record LoomCloudRecipe(String mainId, String auxiliaryId, String outputId, int materialLevel)
            implements CraftRecipeDefinition {
        public static final MapCodec<LoomCloudRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("main_id").forGetter(LoomCloudRecipe::mainId),
                Codec.STRING.fieldOf("auxiliary_id").forGetter(LoomCloudRecipe::auxiliaryId),
                Codec.STRING.fieldOf("output_id").forGetter(LoomCloudRecipe::outputId),
                Codec.INT.fieldOf("material_level").forGetter(LoomCloudRecipe::materialLevel)
        ).apply(instance, LoomCloudRecipe::new));

        @Override
        public String kind() {
            return "loom_cloud";
        }
    }

    /** 陶轮制坯配方。 */
    record PotteryRecipe(String outputId, String materialId, int cost) implements CraftRecipeDefinition {
        public static final MapCodec<PotteryRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("output_id").forGetter(PotteryRecipe::outputId),
                Codec.STRING.fieldOf("material_id").forGetter(PotteryRecipe::materialId),
                Codec.INT.fieldOf("cost").forGetter(PotteryRecipe::cost)
        ).apply(instance, PotteryRecipe::new));

        @Override
        public String kind() {
            return "pottery";
        }
    }
}
