package net.yvanchuxiuzhen.system.book.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 文本书籍载体上的正文与分页文本行组件（{@code yvanchuxiuzhen:book_text}）。 */
public record BookTextData(String textDisplay, List<String> lines) {
    public static final BookTextData EMPTY = new BookTextData("", List.of());
    public static final Codec<BookTextData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("text_display", "").forGetter(BookTextData::textDisplay),
            Codec.STRING.listOf().optionalFieldOf("lines", List.of()).forGetter(BookTextData::lines)
    ).apply(instance, BookTextData::new));
    public static final StreamCodec<ByteBuf, BookTextData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public BookTextData {
        lines = List.copyOf(lines);
    }

    public String line(int oneBased) {
        int index = oneBased - 1;
        return index >= 0 && index < lines.size() ? lines.get(index) : "";
    }

    public BookTextData withLines(List<String> updatedLines) {
        return new BookTextData(textDisplay, updatedLines);
    }
}
