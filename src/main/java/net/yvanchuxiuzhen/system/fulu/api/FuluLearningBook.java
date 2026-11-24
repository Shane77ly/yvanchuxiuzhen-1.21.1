package net.yvanchuxiuzhen.system.fulu.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 符箓学习载体书籍的解锁指令与标记组件（{@code yvanchuxiuzhen:fulu_learning}）。 */
public record FuluLearningBook(boolean enabled, String command) {
    public static final FuluLearningBook EMPTY = new FuluLearningBook(false, "");
    public static final Codec<FuluLearningBook> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", false).forGetter(FuluLearningBook::enabled),
            Codec.STRING.optionalFieldOf("command", "").forGetter(FuluLearningBook::command)
    ).apply(instance, FuluLearningBook::new));
    public static final StreamCodec<ByteBuf, FuluLearningBook> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
