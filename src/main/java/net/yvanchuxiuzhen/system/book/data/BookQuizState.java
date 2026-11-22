package net.yvanchuxiuzhen.system.book.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 答题菜单打开期间向本人同步的题号与题数投影；持久计数仍使用玩家实体数据。 */
public record BookQuizState(int questionId, double progress) {
    public static final BookQuizState EMPTY = new BookQuizState(0, 0);
    public static final StreamCodec<ByteBuf, BookQuizState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BookQuizState::questionId,
            ByteBufCodecs.DOUBLE, BookQuizState::progress,
            BookQuizState::new);
}
