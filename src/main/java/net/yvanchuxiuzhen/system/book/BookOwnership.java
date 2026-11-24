package net.yvanchuxiuzhen.system.book;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.book.api.BookTexts;
import net.yvanchuxiuzhen.system.book.api.QuizOwner;

/** 答题完成时写入物品的强类型答题归属组件，不改变所属系统的其他业务组件。 */
public final class BookOwnership {
    private BookOwnership() {
    }

    /** 按来源写入当前物品的答题归属，不要求物品是功法书或技能书。 */
    public static void bindQuizOwner(ServerPlayer player, ItemStack stack) {
        BookTexts.setQuizOwner(stack, new QuizOwner(player.getStringUUID(), player.getDisplayName().getString()));
    }
}
