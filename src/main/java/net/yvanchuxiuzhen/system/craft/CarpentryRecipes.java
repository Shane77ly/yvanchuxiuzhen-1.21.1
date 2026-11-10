package net.yvanchuxiuzhen.system.craft;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** 木匠台两个类别的来源顺序与全部三十七种材料成本，单次制作一件。 */
public final class CarpentryRecipes {
    public static final String BEECH = "榉木";
    public static final String MISCELLANEOUS = "杂物";
    private static final List<Recipe> BEECH_RECIPES = List.of(
            new Recipe("yvanchuxiuzhen:beechfurniture_01", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8))),
            new Recipe("yvanchuxiuzhen:beechfurniture_02", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8))),
            new Recipe("yvanchuxiuzhen:beechfurniture_03", List.of(new Cost("yvanchuxiuzhen:beechplanks", 12))),
            new Recipe("yvanchuxiuzhen:beechfurniture_04", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8))),
            new Recipe("yvanchuxiuzhen:beechfurniture_05", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_06", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_07", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4), new Cost("minecraft:hay_block", 1))),
            new Recipe("yvanchuxiuzhen:beechfurniture_08", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_09", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_10", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_11", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_12", List.of(new Cost("yvanchuxiuzhen:beechplanks", 12))),
            new Recipe("yvanchuxiuzhen:beechfurniture_13", List.of(new Cost("yvanchuxiuzhen:beechplanks", 12))),
            new Recipe("yvanchuxiuzhen:beechfurniture_14", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8))),
            new Recipe("yvanchuxiuzhen:beechfurniture_1501", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_1601", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_1701", List.of(new Cost("yvanchuxiuzhen:beechplanks", 6))),
            new Recipe("yvanchuxiuzhen:beechfurniture_18", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8))),
            new Recipe("yvanchuxiuzhen:beechfurniture_19", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8), new Cost("yvanchuxiuzhen:book_005", 2))),
            new Recipe("yvanchuxiuzhen:beechfurniture_20", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8))),
            new Recipe("yvanchuxiuzhen:beechfurniture_21", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_22", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_23", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_24", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4))),
            new Recipe("yvanchuxiuzhen:beechfurniture_2501", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8), new Cost("minecraft:paper", 2))),
            new Recipe("yvanchuxiuzhen:beechfurniture_26", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8), new Cost("minecraft:paper", 2))),
            new Recipe("yvanchuxiuzhen:beechfurniture_2701", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4), new Cost("minecraft:paper", 1))),
            new Recipe("yvanchuxiuzhen:beechfurniture_2702", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4), new Cost("minecraft:paper", 1))),
            new Recipe("yvanchuxiuzhen:beechfurniture_2703", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4), new Cost("minecraft:paper", 1))),
            new Recipe("yvanchuxiuzhen:beechfurniture_2704", List.of(new Cost("yvanchuxiuzhen:beechplanks", 4), new Cost("minecraft:paper", 1))),
            new Recipe("yvanchuxiuzhen:beechfurniture_28", List.of(new Cost("yvanchuxiuzhen:beechplanks", 6))),
            new Recipe("yvanchuxiuzhen:beechfurniture_29", List.of(new Cost("yvanchuxiuzhen:beechplanks", 8))),
            new Recipe("yvanchuxiuzhen:beechfurniture_30", List.of(new Cost("yvanchuxiuzhen:beechplanks", 2), new Cost("minecraft:candle", 1), new Cost("minecraft:paper", 2))),
            new Recipe("yvanchuxiuzhen:beechfurniture_31", List.of(new Cost("yvanchuxiuzhen:beechplanks", 2), new Cost("minecraft:candle", 1), new Cost("minecraft:paper", 2))),
            new Recipe("yvanchuxiuzhen:beechfurniture_32", List.of(new Cost("yvanchuxiuzhen:beechplanks", 2), new Cost("minecraft:candle", 1), new Cost("minecraft:paper", 2))));
    private static final List<Recipe> MISCELLANEOUS_RECIPES = List.of(
            new Recipe("yvanchuxiuzhen:itemdanyaohulu_04", List.of(new Cost("minecraft:oak_planks", 3))),
            new Recipe("yvanchuxiuzhen:itemdanyaohulu_05", List.of(new Cost("minecraft:oak_planks", 6), new Cost("yvanchuxiuzhen:cailiao_174", 1))));

    private CarpentryRecipes() {
    }

    public static List<Recipe> category(String category) {
        return switch (category) {
            case BEECH -> BEECH_RECIPES;
            case MISCELLANEOUS -> MISCELLANEOUS_RECIPES;
            default -> List.of();
        };
    }

    public record Recipe(String itemId, List<Cost> costs) {
        public Item item() {
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        }
    }

    public record Cost(String itemId, int count) {
        public Item item() {
            return BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
        }
    }
}
