package net.yvanchuxiuzhen.system.gongfa;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueStat;
import net.yvanchuxiuzhen.system.gongfa.data.GongfaData;

/**
 * 藏宝功法表（来源 {@code Gongfalist01}–{@code 08}，数据 {@code content/gongfa/techniques.json} 与 {@code technique_lists.json}）：
 * 第 n 表按 1–max 的随机数取一本功法书。随机数没有对应分支时不给书（第 6 表的 9、10）；超出 max 的分支取不到（第 7 表的 12）（Q26）。
 */
public final class TechniqueLibrary {
    private record Row(int list, int roll, TechniqueDefinition definition) {
        static final Codec<Row> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("list").forGetter(Row::list),
                Codec.INT.fieldOf("roll").forGetter(Row::roll),
                TechniqueDefinition.MAP_CODEC.forGetter(Row::definition)
        ).apply(instance, Row::new));
    }

    private record ListRow(int list, int max) {
        static final Codec<ListRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("list").forGetter(ListRow::list),
                Codec.INT.fieldOf("max").forGetter(ListRow::max)
        ).apply(instance, ListRow::new));
    }

    private static final Map<Integer, Map<Integer, TechniqueDefinition>> ENTRIES = new HashMap<>();
    private static final Map<Integer, Integer> MAXIMA = new HashMap<>();

    static {
        for (Row row : ContentFiles.read("content/gongfa/techniques.json", Row.CODEC)) {
            ENTRIES.computeIfAbsent(row.list(), list -> new HashMap<>()).put(row.roll(), row.definition());
        }
        for (ListRow row : ContentFiles.read("content/gongfa/technique_lists.json", ListRow.CODEC)) {
            MAXIMA.put(row.list(), row.max());
        }
    }

    private TechniqueLibrary() {
    }

    /** 全部功法定义，按表与随机数的顺序。 */
    public static List<TechniqueDefinition> all() {
        return ENTRIES.keySet().stream().sorted()
                .flatMap(list -> ENTRIES.get(list).keySet().stream().sorted().map(roll -> ENTRIES.get(list).get(roll)))
                .toList();
    }

    /** 藏宝等级对应的表：8 以上为第 8 表，依次到 1 为第 1 表；不足 1 时没有书。 */
    public static ItemStack roll(double treasureLevel, RandomSource random) {
        int list = (int) Math.min(Math.floor(treasureLevel), 8);
        if (list < 1) {
            return ItemStack.EMPTY;
        }
        TechniqueDefinition definition = ENTRIES.get(list).get(Mth.nextInt(random, 1, MAXIMA.get(list)));
        return definition == null ? ItemStack.EMPTY : book(definition);
    }

    /** 未研习的功法书：物品取定义的书，显示名为功法名（来源 {@code setHoverName}）。 */
    public static ItemStack book(TechniqueDefinition definition) {
        ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(definition.item()));
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(definition.name()));
        stack.set(GongfaData.TECHNIQUE.get(), TechniqueBook.unstudied(definition));
        return stack;
    }

    /** 按来源 {@code TechniqueGOProcedure} 的 0–22 号参数生成未研习功法书。 */
    public static ItemStack fromArguments(List<String> arguments) {
        String itemId = argument(arguments, 0).toLowerCase(Locale.ENGLISH);
        if (itemId.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ResourceLocation itemKey = itemKey(itemId);
        Item item = BuiltInRegistries.ITEM.get(itemKey);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        List<String> tiers = new ArrayList<>();
        for (int index = 0; index < TechniqueStat.values().length; index++) {
            tiers.add(index < 12 ? argument(arguments, 11 + index) : "");
        }
        Map<String, Double> elements = Map.of(
                "gold", number(arguments, 6),
                "water", number(arguments, 7),
                "tree", number(arguments, 8),
                "fire", number(arguments, 9),
                "soil", number(arguments, 10));
        TechniqueDefinition definition = new TechniqueDefinition(itemKey, argument(arguments, 1),
                gradeText(argument(arguments, 2), number(arguments, 2)), number(arguments, 2),
                levelText(argument(arguments, 3), number(arguments, 3)), number(arguments, 3),
                argument(arguments, 4), argument(arguments, 5), elements, List.copyOf(tiers));
        return book(definition);
    }

    private static ResourceLocation itemKey(String itemId) {
        return ResourceLocation.parse(itemId);
    }

    private static String argument(List<String> arguments, int index) {
        return index < arguments.size() ? arguments.get(index) : "";
    }

    private static double number(List<String> arguments, int index) {
        try {
            return Double.parseDouble(argument(arguments, index).trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static String gradeText(String raw, double value) {
        if (value >= 31) {
            return switch (raw) {
                case "31" -> "天阶一级";
                case "32" -> "天阶二级";
                case "33" -> "天阶三级";
                case "34" -> "天阶四级";
                case "35" -> "天阶五级";
                case "36" -> "天阶六级";
                case "37" -> "天阶七级";
                case "38" -> "天阶八级";
                case "39" -> "天阶九级";
                case "40" -> "天阶十级";
                default -> "超品";
            };
        }
        if (value >= 21) {
            return numberedText(raw, 21, "地阶");
        }
        if (value >= 11) {
            return numberedText(raw, 11, "玄阶");
        }
        return switch (raw) {
            case "0" -> "无品阶";
            case "1" -> "黄阶一级";
            case "2" -> "黄阶二级";
            case "3" -> "黄阶三级";
            case "4" -> "黄阶四级";
            case "5" -> "黄阶五级";
            case "6" -> "黄阶六级";
            case "7" -> "黄阶七级";
            case "8" -> "黄阶八级";
            case "9" -> "黄阶九级";
            case "10" -> "黄阶十级";
            default -> "";
        };
    }

    private static String levelText(String raw, double value) {
        if (value >= 31) {
            return switch (raw) {
                case "31" -> "元婴一重";
                case "32" -> "元婴二重";
                case "33" -> "元婴三重";
                case "34" -> "元婴四重";
                case "35" -> "元婴五重";
                case "36" -> "元婴六重";
                case "37" -> "元婴七重";
                case "38" -> "元婴八重";
                case "39" -> "元婴九重";
                case "40" -> "元婴十重";
                default -> "化神";
            };
        }
        if (value >= 21) {
            return numberedText(raw, 21, "结丹", "转");
        }
        if (value >= 11) {
            return numberedText(raw, 11, "筑基", "阶");
        }
        return switch (raw) {
            case "0" -> "凡人";
            case "1" -> "练气一层";
            case "2" -> "练气二层";
            case "3" -> "练气三层";
            case "4" -> "练气四层";
            case "5" -> "练气五层";
            case "6" -> "练气六层";
            case "7" -> "练气七层";
            case "8" -> "练气八层";
            case "9" -> "练气九层";
            case "10" -> "练气圆满";
            default -> "";
        };
    }

    private static String numberedText(String raw, int start, String prefix) {
        return numberedText(raw, start, prefix, "级");
    }

    private static String numberedText(String raw, int start, String prefix, String suffix) {
        String[] digits = {"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        try {
            int index = Integer.parseInt(raw) - start;
            return index >= 0 && index < digits.length && raw.equals(Integer.toString(start + index))
                    ? prefix + digits[index] + suffix : "";
        } catch (NumberFormatException exception) {
            return "";
        }
    }
}
