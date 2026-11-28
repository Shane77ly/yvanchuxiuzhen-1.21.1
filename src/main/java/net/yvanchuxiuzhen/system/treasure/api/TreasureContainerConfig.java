package net.yvanchuxiuzhen.system.treasure.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 宝箱容器物品与宝箱配置工具上的强类型奖池配置组件（{@code yvanchuxiuzhen:treasure_container}）。 */
public record TreasureContainerConfig(String category, String subtype, double level, double minimum, double maximum) {
    public static final TreasureContainerConfig EMPTY = new TreasureContainerConfig("", "", 0, 0, 0);
    public static final TreasureContainerConfig DEFAULT_DISPLAY = new TreasureContainerConfig("书架", "书桶", 1, 1, 3);
    public static final Codec<TreasureContainerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("category", "").forGetter(TreasureContainerConfig::category),
            Codec.STRING.optionalFieldOf("subtype", "").forGetter(TreasureContainerConfig::subtype),
            Codec.DOUBLE.optionalFieldOf("level", 0.0).forGetter(TreasureContainerConfig::level),
            Codec.DOUBLE.optionalFieldOf("minimum", 0.0).forGetter(TreasureContainerConfig::minimum),
            Codec.DOUBLE.optionalFieldOf("maximum", 0.0).forGetter(TreasureContainerConfig::maximum)
    ).apply(instance, TreasureContainerConfig::new));
    public static final StreamCodec<ByteBuf, TreasureContainerConfig> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isPresent() {
        return !category.isEmpty();
    }
}
