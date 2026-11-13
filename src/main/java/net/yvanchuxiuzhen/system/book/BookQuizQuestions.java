package net.yvanchuxiuzhen.system.book;

import java.util.List;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 1.18.2 版本 Shengchengtimu 的四十道固定题目；题目顺序即来源随机数 1–40 的编号。 */
public final class BookQuizQuestions {
    private static final List<BookQuizQuestion> QUESTIONS = load();

    private BookQuizQuestions() {
    }

    public static BookQuizQuestion get(int id) {
        return id == 0 ? BookQuizQuestion.EMPTY : QUESTIONS.get(id - 1);
    }

    public static int count() {
        return QUESTIONS.size();
    }

    private static List<BookQuizQuestion> load() {
        List<BookQuizQuestion> questions = ContentFiles.read("content/book/quiz_questions.json", BookQuizQuestion.CODEC);
        if (questions.size() != 40) {
            throw new IllegalStateException("答题题库应包含四十题，实际为：" + questions.size());
        }
        for (int index = 0; index < questions.size(); index++) {
            if (questions.get(index).id() != index + 1) {
                throw new IllegalStateException("答题编号与题库顺序不符：" + (index + 1));
            }
        }
        return List.copyOf(questions);
    }
}
