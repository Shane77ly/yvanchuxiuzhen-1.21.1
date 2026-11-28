package net.yvanchuxiuzhen.system.dungeon.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

/** 副本强类型定义与奖励/波次 Codec，装载自 {@code content/dungeon/dungeons.json}。 */
public record DungeonDefinition(
        String id,
        String name,
        int timeLimit,
        Optional<BlockPos> towerPos,
        List<DungeonRewardEntry> rewards,
        List<List<DungeonMobSpawn>> waves
) {
    public static final DungeonDefinition EMPTY = new DungeonDefinition("", "", 0, Optional.empty(), List.of(), List.of());

    public static final Codec<DungeonDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(DungeonDefinition::id),
            Codec.STRING.fieldOf("name").forGetter(DungeonDefinition::name),
            Codec.INT.fieldOf("time_limit").forGetter(DungeonDefinition::timeLimit),
            BlockPos.CODEC.optionalFieldOf("tower_pos").forGetter(DungeonDefinition::towerPos),
            DungeonRewardEntry.CODEC.listOf().optionalFieldOf("rewards", List.of()).forGetter(DungeonDefinition::rewards),
            DungeonMobSpawn.CODEC.listOf().listOf().optionalFieldOf("waves", List.of()).forGetter(DungeonDefinition::waves)
    ).apply(instance, DungeonDefinition::new));

    public record DungeonMobSpawn(ResourceLocation entityId, int level, String markerId) {
        public static final Codec<DungeonMobSpawn> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("entity_id").forGetter(DungeonMobSpawn::entityId),
                Codec.INT.fieldOf("level").forGetter(DungeonMobSpawn::level),
                Codec.STRING.fieldOf("marker_id").forGetter(DungeonMobSpawn::markerId)
        ).apply(instance, DungeonMobSpawn::new));
    }

    public record DungeonRewardEntry(
            ResourceLocation itemId,
            String customName,
            int weight,
            int count,
            RewardPayload payload
    ) {
        public static final Codec<DungeonRewardEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item_id").forGetter(DungeonRewardEntry::itemId),
                Codec.STRING.optionalFieldOf("custom_name", "").forGetter(DungeonRewardEntry::customName),
                Codec.INT.fieldOf("weight").forGetter(DungeonRewardEntry::weight),
                Codec.INT.fieldOf("count").forGetter(DungeonRewardEntry::count),
                RewardPayload.CODEC.fieldOf("payload").forGetter(DungeonRewardEntry::payload)
        ).apply(instance, DungeonRewardEntry::new));
    }

    public sealed interface RewardPayload permits ItemPayload, SkillPayload, BlueprintPayload, MedicineRecipePayload, TreasurePayload {
        Codec<RewardPayload> CODEC = Codec.STRING.dispatch("type", RewardPayload::type, type -> switch (type) {
            case "item" -> ItemPayload.MAP_CODEC;
            case "skill" -> SkillPayload.MAP_CODEC;
            case "blueprint" -> BlueprintPayload.MAP_CODEC;
            case "medicine_recipe" -> MedicineRecipePayload.MAP_CODEC;
            case "treasure" -> TreasurePayload.MAP_CODEC;
            default -> throw new IllegalArgumentException("未知副本奖励载体类型：" + type);
        });

        String type();
    }

    public record ItemPayload(int materialLevel, int materialGrade) implements RewardPayload {
        public static final MapCodec<ItemPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.optionalFieldOf("material_level", 0).forGetter(ItemPayload::materialLevel),
                Codec.INT.optionalFieldOf("material_grade", 0).forGetter(ItemPayload::materialGrade)
        ).apply(instance, ItemPayload::new));

        @Override public String type() { return "item"; }
    }

    public record SkillPayload(
            String name,
            double grade,
            double level,
            String body,
            String intro,
            Map<String, Double> numbers,
            Map<String, String> texts
    ) implements RewardPayload {
        public static final MapCodec<SkillPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(SkillPayload::name),
                Codec.DOUBLE.fieldOf("grade").forGetter(SkillPayload::grade),
                Codec.DOUBLE.fieldOf("level").forGetter(SkillPayload::level),
                Codec.STRING.fieldOf("body").forGetter(SkillPayload::body),
                Codec.STRING.fieldOf("intro").forGetter(SkillPayload::intro),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("numbers", Map.of()).forGetter(SkillPayload::numbers),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("texts", Map.of()).forGetter(SkillPayload::texts)
        ).apply(instance, SkillPayload::new));

        @Override public String type() { return "skill"; }
    }

    public record BlueprintIngredient(String id, String name, int count) {
        public static final Codec<BlueprintIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(BlueprintIngredient::id),
                Codec.STRING.fieldOf("name").forGetter(BlueprintIngredient::name),
                Codec.INT.fieldOf("count").forGetter(BlueprintIngredient::count)
        ).apply(instance, BlueprintIngredient::new));
    }

    public record BlueprintPayload(
            String name,
            String description,
            String outputItemId,
            String outputDescription,
            String outputType,
            String itemLevelText,
            int itemLevelNum,
            List<BlueprintIngredient> ingredients
    ) implements RewardPayload {
        public static final MapCodec<BlueprintPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(BlueprintPayload::name),
                Codec.STRING.fieldOf("description").forGetter(BlueprintPayload::description),
                Codec.STRING.fieldOf("output_item_id").forGetter(BlueprintPayload::outputItemId),
                Codec.STRING.fieldOf("output_description").forGetter(BlueprintPayload::outputDescription),
                Codec.STRING.fieldOf("output_type").forGetter(BlueprintPayload::outputType),
                Codec.STRING.fieldOf("item_level_text").forGetter(BlueprintPayload::itemLevelText),
                Codec.INT.fieldOf("item_level_num").forGetter(BlueprintPayload::itemLevelNum),
                BlueprintIngredient.CODEC.listOf().fieldOf("ingredients").forGetter(BlueprintPayload::ingredients)
        ).apply(instance, BlueprintPayload::new));

        @Override public String type() { return "blueprint"; }
    }

    public record MedicineIngredient(String id, String name) {
        public static final Codec<MedicineIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(MedicineIngredient::id),
                Codec.STRING.fieldOf("name").forGetter(MedicineIngredient::name)
        ).apply(instance, MedicineIngredient::new));
    }

    public record MedicineRecipePayload(
            String name,
            String recipeDescription,
            String medicineDescription,
            String itemLevelText,
            int itemLevelNum,
            List<MedicineIngredient> ingredients
    ) implements RewardPayload {
        public static final MapCodec<MedicineRecipePayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(MedicineRecipePayload::name),
                Codec.STRING.fieldOf("recipe_description").forGetter(MedicineRecipePayload::recipeDescription),
                Codec.STRING.fieldOf("medicine_description").forGetter(MedicineRecipePayload::medicineDescription),
                Codec.STRING.fieldOf("item_level_text").forGetter(MedicineRecipePayload::itemLevelText),
                Codec.INT.fieldOf("item_level_num").forGetter(MedicineRecipePayload::itemLevelNum),
                MedicineIngredient.CODEC.listOf().fieldOf("ingredients").forGetter(MedicineRecipePayload::ingredients)
        ).apply(instance, MedicineRecipePayload::new));

        @Override public String type() { return "medicine_recipe"; }
    }

    public record TreasurePayload(String category, String subtype, double level, double minimum, double maximum) implements RewardPayload {
        public static final MapCodec<TreasurePayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("category").forGetter(TreasurePayload::category),
                Codec.STRING.fieldOf("subtype").forGetter(TreasurePayload::subtype),
                Codec.DOUBLE.fieldOf("level").forGetter(TreasurePayload::level),
                Codec.DOUBLE.fieldOf("minimum").forGetter(TreasurePayload::minimum),
                Codec.DOUBLE.fieldOf("maximum").forGetter(TreasurePayload::maximum)
        ).apply(instance, TreasurePayload::new));

        @Override public String type() { return "treasure"; }
    }
}
