package net.yvanchuxiuzhen.system.xiulian.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 出身石盘记录的追踪状态与目标坐标（{@code yvanchuxiuzhen:spirit_disc}）。 */
public record SpiritDiscData(String state, double targetX, double targetY, double targetZ) {
    public static final SpiritDiscData EMPTY = new SpiritDiscData("", 0, 0, 0);
    public static final Codec<SpiritDiscData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("state", "").forGetter(SpiritDiscData::state),
            Codec.DOUBLE.optionalFieldOf("target_x", 0.0).forGetter(SpiritDiscData::targetX),
            Codec.DOUBLE.optionalFieldOf("target_y", 0.0).forGetter(SpiritDiscData::targetY),
            Codec.DOUBLE.optionalFieldOf("target_z", 0.0).forGetter(SpiritDiscData::targetZ)
    ).apply(instance, SpiritDiscData::new));
    public static final StreamCodec<ByteBuf, SpiritDiscData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public SpiritDiscData withState(String nextState) {
        return new SpiritDiscData(nextState, targetX, targetY, targetZ);
    }
}
