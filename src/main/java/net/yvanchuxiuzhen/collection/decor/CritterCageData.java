package net.yvanchuxiuzhen.collection.decor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 兔笼收取动物时保存的数量与毛色变种（{@code yvanchuxiuzhen:critter_cage}）。 */
public record CritterCageData(int rabbitCount, int rabbitVariant, int chickenCount) {
    public static final CritterCageData EMPTY = new CritterCageData(0, 0, 0);
    public static final Codec<CritterCageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("rabbit_count", 0).forGetter(CritterCageData::rabbitCount),
            Codec.INT.optionalFieldOf("rabbit_variant", 0).forGetter(CritterCageData::rabbitVariant),
            Codec.INT.optionalFieldOf("chicken_count", 0).forGetter(CritterCageData::chickenCount)
    ).apply(instance, CritterCageData::new));
    public static final StreamCodec<ByteBuf, CritterCageData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
