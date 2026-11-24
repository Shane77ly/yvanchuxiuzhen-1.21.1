package net.yvanchuxiuzhen.collection.festival;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 手持烟花的点燃状态与剩余燃放刻（{@code yvanchuxiuzhen:firework_state}）。 */
public record HandheldFireworkState(boolean lit, double ticks) {
    public static final HandheldFireworkState UNLIT = new HandheldFireworkState(false, 0);
    public static final Codec<HandheldFireworkState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("lit", false).forGetter(HandheldFireworkState::lit),
            Codec.DOUBLE.optionalFieldOf("ticks", 0.0).forGetter(HandheldFireworkState::ticks)
    ).apply(instance, HandheldFireworkState::new));
    public static final StreamCodec<ByteBuf, HandheldFireworkState> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
