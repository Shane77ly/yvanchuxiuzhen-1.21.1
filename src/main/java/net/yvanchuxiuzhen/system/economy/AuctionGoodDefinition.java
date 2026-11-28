package net.yvanchuxiuzhen.system.economy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.system.economy.data.AuctionLots;

/** 拍卖拍品强类型 JSON 定义，替代旧版斜线三元组文本导入。 */
record AuctionGoodDefinition(int tier,
                                   ResourceLocation item,
                                   Optional<Integer> materialLevel,
                                   Optional<Integer> materialQuality,
                                   List<AuctionLots.Entry> lots) {
    static final Codec<AuctionGoodDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("tier").forGetter(AuctionGoodDefinition::tier),
            ResourceLocation.CODEC.fieldOf("item").forGetter(AuctionGoodDefinition::item),
            Codec.INT.optionalFieldOf("material_level").forGetter(AuctionGoodDefinition::materialLevel),
            Codec.INT.optionalFieldOf("material_quality").forGetter(AuctionGoodDefinition::materialQuality),
            AuctionLots.Entry.CODEC.listOf().optionalFieldOf("lots", List.of()).forGetter(AuctionGoodDefinition::lots)
    ).apply(instance, AuctionGoodDefinition::new));

    AuctionGoodDefinition {
        lots = List.copyOf(lots);
    }
}
