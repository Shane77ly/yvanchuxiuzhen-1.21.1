package net.yvanchuxiuzhen.system.book;

import java.util.Random;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import net.yvanchuxiuzhen.system.skill.api.Skills;

/** 答题服务端规则：当前主手接受答满结果，玩家实体保存累计数，菜单关闭不清题目或计数。 */
public final class BookQuiz {
    private BookQuiz() {
    }

    /** 来源 Shengchengtimu 每次重新创建随机源，均匀抽取一至四十，允许连续重复同一道题。 */
    public static void nextQuestion(ServerPlayer player) {
        player.setData(BookQuizData.QUESTION, new Random().nextInt(BookQuizQuestions.count()) + 1);
        BookQuizData.publish(player);
    }

    public static void answer(ServerPlayer player, int option) {
        BookQuizQuestion question = BookQuizQuestions.get(player.getData(BookQuizData.QUESTION));
        if (question.answer() == option) {
            player.serverLevel().sendParticles(Particles.type("tiangandizhi"), player.getX() + 0.5, player.getY() + 1,
                    player.getZ() + 0.5, 10, 0, 0, 0, 0.2);
            double progress = BookQuizData.answerCount(player) + 1;
            BookQuizData.setAnswerCount(player, progress);
            if (progress > 10) {
                complete(player);
            } else {
                nextQuestion(player);
            }
        } else {
            player.closeContainer();
            player.serverLevel().sendParticles(ParticleTypes.ENCHANTED_HIT, player.getX() + 0.5, player.getY() + 1,
                    player.getZ() + 0.5, 20, 0, 0, 0, 0.2);
            player.setHealth((float) (player.getHealth() - player.getMaxHealth() * 0.1));
            BookQuizData.setAnswerCount(player, 0);
            BookQuizData.publish(player);
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.book_quiz_failed"), false);
        }
    }

    private static void complete(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();
        // 来源答满直接覆盖归属，不重新检查境界、灵根或原研习者，也不附赠自然研习才发放的技能。
        BookOwnership.bindQuizOwner(player, stack);
        if (Techniques.book(stack).isPresent()) {
            Techniques.completeQuiz(player, stack);
        }
        if (Skills.book(stack).isPresent()) {
            Skills.completeQuiz(player, stack);
        }
        player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.book_quiz_complete"), false);
        BookQuizData.setAnswerCount(player, 0);
        BookQuizData.publish(player);
        player.inventoryMenu.broadcastChanges();
        player.closeContainer();
    }
}
