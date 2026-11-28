package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 飞行法器物品与实体之间双向同步的强类型实例组件（{@code yvanchuxiuzhen:flying_instrument}）。 */
public record FlyingInstrumentData(String itemId, String entityId, String name,
                                   double healthMax, double health,
                                   double powerMax, double power, double speed) {
    public static final FlyingInstrumentData EMPTY = new FlyingInstrumentData("", "", "", 0, 0, 0, 0, 0);
    public static final Codec<FlyingInstrumentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("item_id", "").forGetter(FlyingInstrumentData::itemId),
            Codec.STRING.optionalFieldOf("entity_id", "").forGetter(FlyingInstrumentData::entityId),
            Codec.STRING.optionalFieldOf("name", "").forGetter(FlyingInstrumentData::name),
            Codec.DOUBLE.optionalFieldOf("health_max", 0.0).forGetter(FlyingInstrumentData::healthMax),
            Codec.DOUBLE.optionalFieldOf("health", 0.0).forGetter(FlyingInstrumentData::health),
            Codec.DOUBLE.optionalFieldOf("power_max", 0.0).forGetter(FlyingInstrumentData::powerMax),
            Codec.DOUBLE.optionalFieldOf("power", 0.0).forGetter(FlyingInstrumentData::power),
            Codec.DOUBLE.optionalFieldOf("speed", 0.0).forGetter(FlyingInstrumentData::speed)
    ).apply(instance, FlyingInstrumentData::new));
    public static final StreamCodec<ByteBuf, FlyingInstrumentData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isPresent() {
        return !entityId.isEmpty() || !itemId.isEmpty() || !name.isEmpty() || healthMax > 0 || powerMax > 0;
    }

    public FlyingInstrumentData withHealth(double newHealth) {
        return new FlyingInstrumentData(itemId, entityId, name, healthMax, newHealth, powerMax, power, speed);
    }
}
