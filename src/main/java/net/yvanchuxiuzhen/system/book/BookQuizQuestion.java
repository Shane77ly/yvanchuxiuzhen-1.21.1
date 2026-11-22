package net.yvanchuxiuzhen.system.book;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

/** 来源答题题库中的一道固定题目、四个选项及正确选项序号。 */
public record BookQuizQuestion(int id, String prompt, List<String> options, int answer) {
    public static final BookQuizQuestion EMPTY = new BookQuizQuestion(0, "\"\"", List.of("\"\"", "\"\"", "\"\"", "\"\""), 0);
    public static final Codec<BookQuizQuestion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 40).fieldOf("id").forGetter(BookQuizQuestion::id),
            Codec.STRING.fieldOf("prompt").forGetter(BookQuizQuestion::prompt),
            Codec.STRING.listOf(4, 4).fieldOf("options").forGetter(BookQuizQuestion::options),
            Codec.intRange(1, 4).fieldOf("answer").forGetter(BookQuizQuestion::answer)
    ).apply(instance, BookQuizQuestion::new));
}
