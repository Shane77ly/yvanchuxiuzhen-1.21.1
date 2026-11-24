package net.yvanchuxiuzhen.system.book.api;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;

/** 由书籍所有者构造并读写文本书籍与通用答题归属组件。 */
public final class BookTexts {
    private BookTexts() {
    }

    public static BookTextData get(ItemStack stack) {
        return stack.getOrDefault(BookQuizData.TEXT.get(), BookTextData.EMPTY);
    }

    public static void set(ItemStack stack, BookTextData data) {
        if (data == null || data.textDisplay().isEmpty() && data.lines().isEmpty()) {
            stack.remove(BookQuizData.TEXT.get());
        } else {
            stack.set(BookQuizData.TEXT.get(), data);
        }
    }

    public static QuizOwner quizOwner(ItemStack stack) {
        return stack.getOrDefault(BookQuizData.QUIZ_OWNER.get(), QuizOwner.EMPTY);
    }

    public static void setQuizOwner(ItemStack stack, QuizOwner owner) {
        if (owner == null || !owner.isPresent() && owner.ownerName().isEmpty()) {
            stack.remove(BookQuizData.QUIZ_OWNER.get());
        } else {
            stack.set(BookQuizData.QUIZ_OWNER.get(), owner);
        }
    }

    public static ItemStack create(Item carrier, String name, String text) {
        ItemStack stack = new ItemStack(carrier);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        set(stack, new BookTextData(text, List.of()));
        return stack;
    }
}
