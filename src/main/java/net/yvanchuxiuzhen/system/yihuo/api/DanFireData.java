package net.yvanchuxiuzhen.system.yihuo.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 丹火物品携带的施放者与火力强度数据（{@code yvanchuxiuzhen:dan_fire}）。 */
public record DanFireData(String ownerUuid, double power) {
    public static final DanFireData EMPTY = new DanFireData("", 0);
    public static final Codec<DanFireData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(DanFireData::ownerUuid),
            Codec.DOUBLE.optionalFieldOf("power", 0.0).forGetter(DanFireData::power)
    ).apply(instance, DanFireData::new));
    public static final StreamCodec<ByteBuf, DanFireData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
