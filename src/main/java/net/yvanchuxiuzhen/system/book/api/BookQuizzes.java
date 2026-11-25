package net.yvanchuxiuzhen.system.book.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;

/** 答题系统的服务端状态操作。 */
public final class BookQuizzes {
    private BookQuizzes() {
    }

    /** 轮回清题号 gongfaxvexi，保留实体上的答对计数。 */
    public static void clearQuestion(ServerPlayer player) {
        player.setData(BookQuizData.QUESTION, 0);
        BookQuizData.publish(player);
    }

    public static double answerCount(Player player) {
        return BookQuizData.answerCount(player);
    }

    public static void setAnswerCount(Player player, double count) {
        BookQuizData.setAnswerCount(player, count);
        BookQuizData.publish(player);
    }
}
