package net.yvanchuxiuzhen.collection.decor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 建筑笔物品记录的屋顶形制与瓦片风格（{@code yvanchuxiuzhen:roof_brush}）。 */
public record RoofBrushConfig(String roofType, String tileType) {
    public static final RoofBrushConfig DEFAULT = new RoofBrushConfig("", "");
    public static final Codec<RoofBrushConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("roof_type", "").forGetter(RoofBrushConfig::roofType),
            Codec.STRING.optionalFieldOf("tile_type", "").forGetter(RoofBrushConfig::tileType)
    ).apply(instance, RoofBrushConfig::new));
    public static final StreamCodec<ByteBuf, RoofBrushConfig> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
