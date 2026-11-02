package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

/**
 * 物品内容表的一行（{@code content/<单位>/items.json}）。factory 为 {@code item} 时是普通物品，
 * 为 {@code block_item} 时是方块物品，block 指向同一单位的方块，double 表示高两格的方块物品。
 * order 为来源注册顺序，创造栏据此排序。
 */
public record ItemRow(String id, String factory, int order, Optional<String> block, Optional<Boolean> doubleHigh,
                      Optional<String> tab, Optional<Integer> stack, Optional<String> rarity,
                      Optional<Boolean> fireResistant, Optional<Integer> durability, Optional<Food> food,
                      Optional<List<String>> tooltip, Optional<List<String>> pendingHooks) {
    public static final Codec<ItemRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(ItemRow::id),
            Codec.STRING.fieldOf("factory").forGetter(ItemRow::factory),
            Codec.INT.fieldOf("order").forGetter(ItemRow::order),
            Codec.STRING.optionalFieldOf("block").forGetter(ItemRow::block),
            Codec.BOOL.optionalFieldOf("double").forGetter(ItemRow::doubleHigh),
            Codec.STRING.optionalFieldOf("tab").forGetter(ItemRow::tab),
            Codec.INT.optionalFieldOf("stack").forGetter(ItemRow::stack),
            Codec.STRING.optionalFieldOf("rarity").forGetter(ItemRow::rarity),
            Codec.BOOL.optionalFieldOf("fire_resistant").forGetter(ItemRow::fireResistant),
            Codec.INT.optionalFieldOf("durability").forGetter(ItemRow::durability),
            Food.CODEC.optionalFieldOf("food").forGetter(ItemRow::food),
            Codec.STRING.listOf().optionalFieldOf("tooltip").forGetter(ItemRow::tooltip),
            Codec.STRING.listOf().optionalFieldOf("pending_hooks").forGetter(ItemRow::pendingHooks)
    ).apply(instance, ItemRow::new));

    /** 食物：饱食度与来源的饱和度系数（1.18.2 的 saturationMod）。 */
    public record Food(int nutrition, float saturationMod, Optional<Boolean> alwaysEat, Optional<Boolean> meat,
                       Optional<Integer> useTicks) {
        static final Codec<Food> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("nutrition").forGetter(Food::nutrition),
                Codec.FLOAT.fieldOf("saturation_mod").forGetter(Food::saturationMod),
                Codec.BOOL.optionalFieldOf("always_eat").forGetter(Food::alwaysEat),
                Codec.BOOL.optionalFieldOf("meat").forGetter(Food::meat),
                Codec.INT.optionalFieldOf("use_ticks").forGetter(Food::useTicks)
        ).apply(instance, Food::new));
    }
}
