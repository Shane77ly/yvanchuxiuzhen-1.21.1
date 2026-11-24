package net.yvanchuxiuzhen.system.world.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 寻灵盘记录的人间界返回坐标（{@code yvanchuxiuzhen:compass_anchor}）。 */
public record CompassAnchor(boolean present, double x, double y, double z) {
    public static final CompassAnchor EMPTY = new CompassAnchor(false, 0, 0, 0);
    public static final Codec<CompassAnchor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("present", false).forGetter(CompassAnchor::present),
            Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(CompassAnchor::x),
            Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(CompassAnchor::y),
            Codec.DOUBLE.optionalFieldOf("z", 0.0).forGetter(CompassAnchor::z)
    ).apply(instance, CompassAnchor::new));
    public static final StreamCodec<ByteBuf, CompassAnchor> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
