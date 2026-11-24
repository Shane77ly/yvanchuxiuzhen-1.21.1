package net.yvanchuxiuzhen.system.cooking.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 酒类物品的陈酿年份组件（{@code yvanchuxiuzhen:wine}）。 */
public record WineData(double ageYears) {
    public static final WineData EMPTY = new WineData(0.0);
    public static final Codec<WineData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("age_years", 0.0).forGetter(WineData::ageYears)
    ).apply(instance, WineData::new));
    public static final StreamCodec<ByteBuf, WineData> STREAM_CODEC = ByteBufCodecs.DOUBLE.map(WineData::new, WineData::ageYears);
}
