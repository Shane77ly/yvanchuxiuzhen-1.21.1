package net.yvanchuxiuzhen.system.economy.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.yvanchuxiuzhen.system.cooking.api.CookingRecipeData;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.liandan.api.MedicineRecipe;
import net.yvanchuxiuzhen.system.liandan.api.MedicineReward;
import net.yvanchuxiuzhen.system.lianqi.api.LianqiBlueprint;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;

/**
 * 商店与 NPC 商品目录的强类型定义，彻底替代旧版 {@code %}/{@code /} 拼接串。
 * 供 {@code content/economy/shop_catalogs.json}、随机材料商店、任务伪装方块及经济菜单会话共用。
 */
public record ShopCatalogDefinition(String id, ShopInfo info, List<Product> products) {
    public static final ShopCatalogDefinition EMPTY = new ShopCatalogDefinition("", ShopInfo.EMPTY, List.of());
    public static final Codec<ShopCatalogDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(ShopCatalogDefinition::id),
            ShopInfo.CODEC.fieldOf("info").forGetter(ShopCatalogDefinition::info),
            Product.CODEC.listOf().fieldOf("products").forGetter(ShopCatalogDefinition::products)
    ).apply(instance, ShopCatalogDefinition::new));

    public ShopCatalogDefinition {
        id = id == null ? "" : id;
        info = info == null ? ShopInfo.EMPTY : info;
        products = products == null ? List.of() : List.copyOf(products);
    }

    /** 商店标题、店主与简介（对应原 {@code store%owner%description} 三元组）。 */
    public record ShopInfo(String storeName, String ownerName, String description) {
        public static final ShopInfo EMPTY = new ShopInfo("", "", "");
        public static final Codec<ShopInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("store_name").forGetter(ShopInfo::storeName),
                Codec.STRING.fieldOf("owner_name").forGetter(ShopInfo::ownerName),
                Codec.STRING.fieldOf("description").forGetter(ShopInfo::description)
        ).apply(instance, ShopInfo::new));

        public boolean isEmpty() {
            return storeName.isEmpty() && ownerName.isEmpty() && description.isEmpty();
        }
    }

    /** 商品结算货币。 */
    public enum Currency implements StringRepresentable {
        STONES("灵石"),
        COINS("金银");

        public static final Codec<Currency> CODEC = StringRepresentable.fromEnum(Currency::values);
        private final String serializedName;

        Currency(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        public boolean isStones() {
            return this == STONES;
        }
    }

    /** 单件商店商品规约。 */
    public record Product(ResourceLocation item,
                          Optional<String> displayName,
                          Currency currency,
                          double price,
                          ItemPayload payload) {
        public static final Codec<Product> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("item").forGetter(Product::item),
                Codec.STRING.optionalFieldOf("display_name").forGetter(Product::displayName),
                Currency.CODEC.fieldOf("currency").forGetter(Product::currency),
                Codec.DOUBLE.fieldOf("price").forGetter(Product::price),
                ItemPayload.CODEC.optionalFieldOf("payload", NoPayload.INSTANCE).forGetter(Product::payload)
        ).apply(instance, Product::new));

        public Product {
            payload = payload == null ? NoPayload.INSTANCE : payload;
        }
    }

    /** 物品附带的强类型业务载荷，供商店商品与任务奖励发物共用。 */
    public sealed interface ItemPayload permits NoPayload, MaterialPayload, TechniquePayload, SkillPayload,
            FuluPayload, FuluLearningPayload, MedicinePayload, RecipePayload, BlueprintPayload, CookingRecipePayload,
            BookTextPayload, TreasureContainerPayload, CasketCommandPayload {
        Codec<ItemPayload> CODEC = Codec.STRING.dispatch("kind", ItemPayload::kind, kind -> switch (kind) {
            case "none" -> NoPayload.MAP_CODEC;
            case "material" -> MaterialPayload.MAP_CODEC;
            case "technique" -> TechniquePayload.MAP_CODEC;
            case "skill" -> SkillPayload.MAP_CODEC;
            case "fulu" -> FuluPayload.MAP_CODEC;
            case "fulu_learning" -> FuluLearningPayload.MAP_CODEC;
            case "medicine" -> MedicinePayload.MAP_CODEC;
            case "recipe" -> RecipePayload.MAP_CODEC;
            case "blueprint" -> BlueprintPayload.MAP_CODEC;
            case "cooking_recipe" -> CookingRecipePayload.MAP_CODEC;
            case "book_text" -> BookTextPayload.MAP_CODEC;
            case "treasure_container" -> TreasureContainerPayload.MAP_CODEC;
            case "casket_command" -> CasketCommandPayload.MAP_CODEC;
            default -> throw new IllegalArgumentException("未知商品/物品载荷类型：" + kind);
        });

        String kind();
    }

    /** 无额外组件载荷。 */
    public record NoPayload() implements ItemPayload {
        public static final NoPayload INSTANCE = new NoPayload();
        public static final MapCodec<NoPayload> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public String kind() {
            return "none";
        }
    }

    /** 材料灵气等级与品阶载荷。 */
    public record MaterialPayload(int level, int quality) implements ItemPayload {
        public static final MapCodec<MaterialPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("level").forGetter(MaterialPayload::level),
                Codec.INT.fieldOf("quality").forGetter(MaterialPayload::quality)
        ).apply(instance, MaterialPayload::new));

        @Override
        public String kind() {
            return "material";
        }
    }

    /** 功法书定义载荷，直接复用功法系统 {@link TechniqueDefinition#MAP_CODEC}。 */
    public record TechniquePayload(TechniqueDefinition definition) implements ItemPayload {
        public static final MapCodec<TechniquePayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TechniqueDefinition.MAP_CODEC.forGetter(TechniquePayload::definition)
        ).apply(instance, TechniquePayload::new));

        @Override
        public String kind() {
            return "technique";
        }
    }

    /** 技能、武器技、身法与轻功书载荷。 */
    public record SkillPayload(SkillKind skillKind,
                               String name,
                               double grade,
                               double level,
                               String body,
                               String intro,
                               Map<String, Double> numbers,
                               Map<String, String> texts) implements ItemPayload {
        public static final MapCodec<SkillPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                SkillKind.CODEC.fieldOf("skill_kind").forGetter(SkillPayload::skillKind),
                Codec.STRING.fieldOf("name").forGetter(SkillPayload::name),
                Codec.DOUBLE.fieldOf("grade").forGetter(SkillPayload::grade),
                Codec.DOUBLE.fieldOf("level").forGetter(SkillPayload::level),
                Codec.STRING.fieldOf("body").forGetter(SkillPayload::body),
                Codec.STRING.fieldOf("intro").forGetter(SkillPayload::intro),
                Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("numbers", Map.of()).forGetter(SkillPayload::numbers),
                Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("texts", Map.of()).forGetter(SkillPayload::texts)
        ).apply(instance, SkillPayload::new));

        public SkillPayload {
            numbers = Map.copyOf(numbers);
            texts = Map.copyOf(texts);
        }

        @Override
        public String kind() {
            return "skill";
        }
    }

    /** 成品符箓等级与品阶名称载荷。 */
    public record FuluPayload(int level, String levelText) implements ItemPayload {
        public static final MapCodec<FuluPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("level").forGetter(FuluPayload::level),
                Codec.STRING.fieldOf("level_text").forGetter(FuluPayload::levelText)
        ).apply(instance, FuluPayload::new));

        @Override
        public String kind() {
            return "fulu";
        }
    }

    /** 符胆学习书载荷。 */
    public record FuluLearningPayload(boolean learnable, String command) implements ItemPayload {
        public static final MapCodec<FuluLearningPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.BOOL.fieldOf("learnable").forGetter(FuluLearningPayload::learnable),
                Codec.STRING.fieldOf("command").forGetter(FuluLearningPayload::command)
        ).apply(instance, FuluLearningPayload::new));

        @Override
        public String kind() {
            return "fulu_learning";
        }
    }

    /** 成品丹药实例载荷，供任务发药等场景使用。 */
    public record MedicinePayload(String name,
                                  int dose,
                                  int packCount,
                                  boolean treasure,
                                  List<MedicineReward.Effect> effects) implements ItemPayload {
        private static final Codec<MedicineReward.Effect> EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(MedicineReward.Effect::id),
                Codec.INT.fieldOf("level").forGetter(MedicineReward.Effect::level),
                Codec.INT.fieldOf("duration").forGetter(MedicineReward.Effect::duration)
        ).apply(instance, MedicineReward.Effect::new));

        public static final MapCodec<MedicinePayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(MedicinePayload::name),
                Codec.INT.fieldOf("dose").forGetter(MedicinePayload::dose),
                Codec.INT.fieldOf("pack_count").forGetter(MedicinePayload::packCount),
                Codec.BOOL.optionalFieldOf("treasure", false).forGetter(MedicinePayload::treasure),
                EFFECT_CODEC.listOf().fieldOf("effects").forGetter(MedicinePayload::effects)
        ).apply(instance, MedicinePayload::new));

        public MedicinePayload {
            effects = List.copyOf(effects);
        }

        @Override
        public String kind() {
            return "medicine";
        }
    }

    /** 丹方书载荷，直接映射 {@link MedicineRecipe}。 */
    public record RecipePayload(String medicineName,
                                String recipeDescription,
                                String medicineDescription,
                                String levelText,
                                int level,
                                String recipeId,
                                List<MedicineRecipe.Ingredient> ingredients) implements ItemPayload {
        private static final Codec<MedicineRecipe.Ingredient> INGREDIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(MedicineRecipe.Ingredient::id),
                Codec.STRING.fieldOf("name").forGetter(MedicineRecipe.Ingredient::name)
        ).apply(instance, MedicineRecipe.Ingredient::new));

        public static final MapCodec<RecipePayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("medicine_name").forGetter(RecipePayload::medicineName),
                Codec.STRING.fieldOf("recipe_description").forGetter(RecipePayload::recipeDescription),
                Codec.STRING.fieldOf("medicine_description").forGetter(RecipePayload::medicineDescription),
                Codec.STRING.fieldOf("level_text").forGetter(RecipePayload::levelText),
                Codec.INT.fieldOf("level").forGetter(RecipePayload::level),
                Codec.STRING.fieldOf("recipe_id").forGetter(RecipePayload::recipeId),
                INGREDIENT_CODEC.listOf().fieldOf("ingredients").forGetter(RecipePayload::ingredients)
        ).apply(instance, RecipePayload::new));

        public RecipePayload {
            ingredients = List.copyOf(ingredients);
        }

        @Override
        public String kind() {
            return "recipe";
        }
    }

    /** 炼器图纸书载荷，直接映射 {@link LianqiBlueprint}。 */
    public record BlueprintPayload(String blueprintName,
                                   String blueprintDescription,
                                   String artifactId,
                                   String artifactDescription,
                                   String artifactType,
                                   String levelText,
                                   int level,
                                   String belongToItemId,
                                   List<LianqiBlueprint.Ingredient> ingredients) implements ItemPayload {
        private static final Codec<LianqiBlueprint.Ingredient> INGREDIENT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(LianqiBlueprint.Ingredient::id),
                Codec.STRING.fieldOf("name").forGetter(LianqiBlueprint.Ingredient::name),
                Codec.INT.fieldOf("count").forGetter(LianqiBlueprint.Ingredient::count)
        ).apply(instance, (id, name, count) -> new LianqiBlueprint.Ingredient(id, name, "", count)));

        public static final MapCodec<BlueprintPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("blueprint_name").forGetter(BlueprintPayload::blueprintName),
                Codec.STRING.fieldOf("blueprint_description").forGetter(BlueprintPayload::blueprintDescription),
                Codec.STRING.fieldOf("artifact_id").forGetter(BlueprintPayload::artifactId),
                Codec.STRING.fieldOf("artifact_description").forGetter(BlueprintPayload::artifactDescription),
                Codec.STRING.fieldOf("artifact_type").forGetter(BlueprintPayload::artifactType),
                Codec.STRING.fieldOf("level_text").forGetter(BlueprintPayload::levelText),
                Codec.INT.fieldOf("level").forGetter(BlueprintPayload::level),
                Codec.STRING.fieldOf("belong_to_item_id").forGetter(BlueprintPayload::belongToItemId),
                INGREDIENT_CODEC.listOf().fieldOf("ingredients").forGetter(BlueprintPayload::ingredients)
        ).apply(instance, BlueprintPayload::new));

        public BlueprintPayload {
            ingredients = List.copyOf(ingredients);
        }

        @Override
        public String kind() {
            return "blueprint";
        }
    }

    /** 烹饪菜谱书载荷，直接映射 {@link CookingRecipeData}。 */
    public record CookingRecipePayload(CookingRecipeData recipe) implements ItemPayload {
        public static final MapCodec<CookingRecipePayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CookingRecipeData.CODEC.fieldOf("recipe").forGetter(CookingRecipePayload::recipe)
        ).apply(instance, CookingRecipePayload::new));

        @Override
        public String kind() {
            return "cooking_recipe";
        }
    }

    /** 阅读文本书载荷。 */
    public record BookTextPayload(String text) implements ItemPayload {
        public static final MapCodec<BookTextPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("text").forGetter(BookTextPayload::text)
        ).apply(instance, BookTextPayload::new));

        @Override
        public String kind() {
            return "book_text";
        }
    }

    /** 商店携带配置参数的宝箱载荷。 */
    public record TreasureContainerPayload(String category,
                                           String subtype,
                                           double level,
                                           double minimum,
                                           double maximum) implements ItemPayload {
        public static final MapCodec<TreasureContainerPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("category").forGetter(TreasureContainerPayload::category),
                Codec.STRING.fieldOf("subtype").forGetter(TreasureContainerPayload::subtype),
                Codec.DOUBLE.fieldOf("level").forGetter(TreasureContainerPayload::level),
                Codec.DOUBLE.fieldOf("minimum").forGetter(TreasureContainerPayload::minimum),
                Codec.DOUBLE.fieldOf("maximum").forGetter(TreasureContainerPayload::maximum)
        ).apply(instance, TreasureContainerPayload::new));

        @Override
        public String kind() {
            return "treasure_container";
        }
    }

    /** 宝匣商品保留的指令参数（阶段 4 或炼器直调清理前保留结构化字段）。 */
    public record CasketCommandPayload(String command) implements ItemPayload {
        public static final MapCodec<CasketCommandPayload> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("command").forGetter(CasketCommandPayload::command)
        ).apply(instance, CasketCommandPayload::new));

        @Override
        public String kind() {
            return "casket_command";
        }
    }
}
