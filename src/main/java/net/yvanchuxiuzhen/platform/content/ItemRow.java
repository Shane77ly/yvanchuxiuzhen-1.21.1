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
                      Optional<List<String>> tooltip, Optional<Tool> tool, Optional<Armor> armor,
                      Optional<Integer> durabilityMultiplier, Optional<List<String>> pendingHooks) {
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
            Tool.CODEC.optionalFieldOf("tool").forGetter(ItemRow::tool),
            Armor.CODEC.optionalFieldOf("armor").forGetter(ItemRow::armor),
            Codec.INT.optionalFieldOf("durability_multiplier").forGetter(ItemRow::durabilityMultiplier),
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

    /** 来源匿名 Tier：耐久、挖掘速度、攻击加成、1.18.2 的等级、附魔值与修复材料。 */
    public record LegacyTierRow(int uses, float speed, float damageBonus, int level, int enchantability,
                                Optional<List<String>> repair) {
        static final Codec<LegacyTierRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("uses").forGetter(LegacyTierRow::uses),
                Codec.FLOAT.fieldOf("speed").forGetter(LegacyTierRow::speed),
                Codec.FLOAT.fieldOf("damage_bonus").forGetter(LegacyTierRow::damageBonus),
                Codec.INT.fieldOf("level").forGetter(LegacyTierRow::level),
                Codec.INT.fieldOf("enchantability").forGetter(LegacyTierRow::enchantability),
                Codec.STRING.listOf().optionalFieldOf("repair").forGetter(LegacyTierRow::repair)
        ).apply(instance, LegacyTierRow::new));
    }

    /** 工具：kind 为 sword、axe、pickaxe、shovel 或 tiered；damage 与 attack_speed 是来源构造器的攻击参数（tiered 没有）。 */
    public record Tool(String kind, LegacyTierRow tier, Optional<Integer> damage, Optional<Float> attackSpeed) {
        static final Codec<Tool> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("kind").forGetter(Tool::kind),
                LegacyTierRow.CODEC.fieldOf("tier").forGetter(Tool::tier),
                Codec.INT.optionalFieldOf("damage").forGetter(Tool::damage),
                Codec.FLOAT.optionalFieldOf("attack_speed").forGetter(Tool::attackSpeed)
        ).apply(instance, Tool::new));
    }

    /** 盔甲部件：material 指向同一单位 armor_materials.json 的材质，slot 为 HELMET、CHESTPLATE、LEGGINGS、BOOTS。 */
    public record Armor(String material, String slot) {
        static final Codec<Armor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("material").forGetter(Armor::material),
                Codec.STRING.fieldOf("slot").forGetter(Armor::slot)
        ).apply(instance, Armor::new));
    }
}
