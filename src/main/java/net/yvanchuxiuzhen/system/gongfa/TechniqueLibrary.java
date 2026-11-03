package net.yvanchuxiuzhen.system.gongfa;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueDefinition;
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
}
