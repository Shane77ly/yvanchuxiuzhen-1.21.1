package net.yvanchuxiuzhen.system.book.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 书籍答题完成时写入物品的通用答题归属组件（{@code yvanchuxiuzhen:quiz_owner}）。 */
public record QuizOwner(String ownerUuid, String ownerName) {
    public static final QuizOwner EMPTY = new QuizOwner("", "");
    public static final Codec<QuizOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("owner_uuid", "").forGetter(QuizOwner::ownerUuid),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(QuizOwner::ownerName)
    ).apply(instance, QuizOwner::new));
    public static final StreamCodec<ByteBuf, QuizOwner> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public boolean isPresent() {
        return !ownerUuid.isEmpty();
    }
}
