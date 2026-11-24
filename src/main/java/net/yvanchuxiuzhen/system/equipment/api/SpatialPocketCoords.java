package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 空间袋跨界法宝存储的双向传送锚点坐标组件（{@code yvanchuxiuzhen:spatial_coords}）。 */
public record SpatialPocketCoords(double x, double y, double z, double targetX, double targetY, double targetZ) {
    public static final SpatialPocketCoords EMPTY = new SpatialPocketCoords(0, 0, 0, 0, 0, 0);
    public static final Codec<SpatialPocketCoords> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("x", 0.0).forGetter(SpatialPocketCoords::x),
            Codec.DOUBLE.optionalFieldOf("y", 0.0).forGetter(SpatialPocketCoords::y),
            Codec.DOUBLE.optionalFieldOf("z", 0.0).forGetter(SpatialPocketCoords::z),
            Codec.DOUBLE.optionalFieldOf("target_x", 0.0).forGetter(SpatialPocketCoords::targetX),
            Codec.DOUBLE.optionalFieldOf("target_y", 0.0).forGetter(SpatialPocketCoords::targetY),
            Codec.DOUBLE.optionalFieldOf("target_z", 0.0).forGetter(SpatialPocketCoords::targetZ)
    ).apply(instance, SpatialPocketCoords::new));
    public static final StreamCodec<ByteBuf, SpatialPocketCoords> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
