package net.yvanchuxiuzhen.system.guchong.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 荧光蛊追踪物品上的强类型追踪坐标与状态组件（{@code yvanchuxiuzhen:guchong_tracking}）。 */
public record GuchongTrackingData(boolean tracking, String ownerUuid,
                                  double targetX, double targetY, double targetZ,
                                  double homeX, double homeY, double homeZ,
                                  boolean tamed, boolean cutPrice) {
    public static final GuchongTrackingData EMPTY = new GuchongTrackingData(false, "", 0, 0, 0, 0, 0, 0, false, false);
    public static final Codec<GuchongTrackingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("tracking", false).forGetter(GuchongTrackingData::tracking),
            Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(GuchongTrackingData::ownerUuid),
            Codec.DOUBLE.optionalFieldOf("target_x", 0.0).forGetter(GuchongTrackingData::targetX),
            Codec.DOUBLE.optionalFieldOf("target_y", 0.0).forGetter(GuchongTrackingData::targetY),
            Codec.DOUBLE.optionalFieldOf("target_z", 0.0).forGetter(GuchongTrackingData::targetZ),
            Codec.DOUBLE.optionalFieldOf("home_x", 0.0).forGetter(GuchongTrackingData::homeX),
            Codec.DOUBLE.optionalFieldOf("home_y", 0.0).forGetter(GuchongTrackingData::homeY),
            Codec.DOUBLE.optionalFieldOf("home_z", 0.0).forGetter(GuchongTrackingData::homeZ),
            Codec.BOOL.optionalFieldOf("tamed", false).forGetter(GuchongTrackingData::tamed),
            Codec.BOOL.optionalFieldOf("cut_price", false).forGetter(GuchongTrackingData::cutPrice)
    ).apply(instance, GuchongTrackingData::new));
    public static final StreamCodec<ByteBuf, GuchongTrackingData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isPresent() {
        return tracking || !ownerUuid.isEmpty() || tamed || cutPrice
                || targetX != 0 || targetY != 0 || targetZ != 0
                || homeX != 0 || homeY != 0 || homeZ != 0;
    }

    public GuchongTrackingData withOwnerUuid(String nextOwnerUuid) {
        return new GuchongTrackingData(tracking, nextOwnerUuid == null ? "" : nextOwnerUuid,
                targetX, targetY, targetZ, homeX, homeY, homeZ, tamed, cutPrice);
    }
}
