package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 精血物品承载的主人与修为信息组件（{@code yvanchuxiuzhen:blood_essence}）。 */
public record BloodEssenceData(String ownerUuid, String ownerName, double levelNumber, String levelName) {
    public static final BloodEssenceData EMPTY = new BloodEssenceData("", "", 0, "");
    public static final Codec<BloodEssenceData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(BloodEssenceData::ownerUuid),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(BloodEssenceData::ownerName),
            Codec.DOUBLE.optionalFieldOf("level_number", 0.0).forGetter(BloodEssenceData::levelNumber),
            Codec.STRING.optionalFieldOf("level_name", "").forGetter(BloodEssenceData::levelName)
    ).apply(instance, BloodEssenceData::new));
    public static final StreamCodec<ByteBuf, BloodEssenceData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
