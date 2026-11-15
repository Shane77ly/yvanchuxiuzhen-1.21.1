package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

/** 大红包的来源随机表：按连续随机值依次比较阈值，再单独抽取掉落件数。 */
final class RedPacketRewards {
    record Reward(ItemStack stack, int count) {
    }

    private record Entry(double threshold, String item, int minimum, int maximum) {
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry(95.0, "yvanchuxiuzhen:money_01", 1, 32),
            new Entry(92.0, "yvanchuxiuzhen:money_02", 1, 10),
            new Entry(91.0, "yvanchuxiuzhen:money_03", 1, 2),
            new Entry(90.0, "yvanchuxiuzhen:money_04", 1, 1),
            new Entry(87.0, "yvanchuxiuzhen:cailiao_190", 1, 1),
            new Entry(86.0, "yvanchuxiuzhen:cailiao_191", 1, 10),
            new Entry(85.0, "yvanchuxiuzhen:cailiao_192", 1, 1),
            new Entry(84.0, "yvanchuxiuzhen:cailiao_221", 1, 1),
            new Entry(81.0, "yvanchuxiuzhen:shicai_039", 1, 10),
            new Entry(76.0, "yvanchuxiuzhen:pao_01", 1, 64),
            new Entry(73.0, "yvanchuxiuzhen:pao_02", 1, 32),
            new Entry(70.0, "yvanchuxiuzhen:pao_03", 1, 16),
            new Entry(68.0, "yvanchuxiuzhen:pao_04", 1, 8),
            new Entry(66.0, "yvanchuxiuzhen:pao_05", 1, 8),
            new Entry(64.0, "yvanchuxiuzhen:pao_06", 1, 8),
            new Entry(62.0, "yvanchuxiuzhen:pao_07", 1, 9),
            new Entry(60.0, "yvanchuxiuzhen:pao_08", 1, 32),
            new Entry(58.0, "yvanchuxiuzhen:itemfulu_15", 1, 1),
            new Entry(55.0, "yvanchuxiuzhen:fuqi_06", 1, 3),
            new Entry(50.0, "yvanchuxiuzhen:fuqi_05", 1, 1),
            new Entry(48.0, "yvanchuxiuzhen:fuqi_01", 1, 1),
            new Entry(45.0, "yvanchuxiuzhen:fuqi_03", 1, 4),
            new Entry(43.0, "yvanchuxiuzhen:pao_09", 1, 16),
            new Entry(42.0, "yvanchuxiuzhen:shicai_003", 1, 8),
            new Entry(41.0, "yvanchuxiuzhen:shicai_001", 1, 8),
            new Entry(40.0, "yvanchuxiuzhen:shicai_002", 1, 8),
            new Entry(38.0, "yvanchuxiuzhen:tiaowei_01", 1, 16),
            new Entry(37.0, "minecraft:gold_ingot", 1, 4),
            new Entry(36.0, "minecraft:diamond", 1, 1),
            new Entry(35.0, "minecraft:gold_nugget", 1, 16),
            new Entry(34.0, "yvanchuxiuzhen:qiankundai_01", 1, 1),
            new Entry(32.0, "minecraft:honey_bottle", 1, 4),
            new Entry(31.0, "minecraft:magma_cream", 1, 4),
            new Entry(30.0, "yvanchuxiuzhen:cailiao_181", 1, 2),
            new Entry(29.0, "minecraft:blaze_rod", 1, 2),
            new Entry(28.0, "minecraft:experience_bottle", 1, 8),
            new Entry(27.0, "minecraft:fire_charge", 1, 4),
            new Entry(25.0, "minecraft:golden_carrot", 1, 4),
            new Entry(23.0, "minecraft:golden_apple", 1, 3),
            new Entry(22.0, "minecraft:enchanted_golden_apple", 1, 1),
            new Entry(21.0, "minecraft:totem_of_undying", 1, 1),
            new Entry(19.0, "minecraft:spyglass", 1, 1),
            new Entry(17.0, "minecraft:clock", 1, 1),
            new Entry(15.0, "minecraft:saddle", 1, 2),
            new Entry(14.0, "yvanchuxiuzhen:cailiao_139", 1, 2),
            new Entry(12.0, "minecraft:lead", 1, 3),
            new Entry(10.0, "minecraft:writable_book", 1, 1),
            new Entry(9.0, "minecraft:dragon_breath", 1, 4),
            new Entry(8.0, "yvanchuxiuzhen:food_017", 1, 8),
            new Entry(7.0, "yvanchuxiuzhen:luopan", 1, 16),
            new Entry(5.0, "minecraft:name_tag", 1, 4),
            new Entry(4.0, "yvanchuxiuzhen:cailiao_231", 1, 1),
            new Entry(3.0, "minecraft:chorus_fruit", 1, 8),
            new Entry(2.0, "minecraft:emerald", 1, 4),
            new Entry(1.5, "yvanchuxiuzhen:cailiao_199", 1, 1),
            new Entry(1.0, "yvanchuxiuzhen:flyinginstrument_0401", 1, 1),
            new Entry(0.5, "yvanchuxiuzhen:fabao_00901", 1, 1),
            new Entry(Double.NEGATIVE_INFINITY, "yvanchuxiuzhen:silkthreadgold", 1, 16)
    );

    private RedPacketRewards() {
    }

    static Reward roll() {
        double roll = Mth.nextDouble(RandomSource.create(), 1, 100);
        Entry entry = ENTRIES.stream().filter(candidate -> roll >= candidate.threshold()).findFirst().orElseThrow();
        int count = entry.minimum() == entry.maximum() ? entry.minimum()
                : Mth.nextInt(RandomSource.create(), entry.minimum(), entry.maximum());
        return new Reward(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.item()))), count);
    }
}
