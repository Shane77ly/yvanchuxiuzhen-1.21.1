package net.yvanchuxiuzhen.system.material.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 材料物品实例的动态等级与品阶，对应来源 {@code cailiaolevel/cailiaopinjie}。 */
public record MaterialInstance(int level, int quality) {
    public static final MaterialInstance EMPTY = new MaterialInstance(0, 0);
    public static final Codec<MaterialInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(MaterialInstance::level),
            Codec.INT.fieldOf("quality").forGetter(MaterialInstance::quality)
    ).apply(instance, MaterialInstance::new));
    public static final StreamCodec<ByteBuf, MaterialInstance> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
