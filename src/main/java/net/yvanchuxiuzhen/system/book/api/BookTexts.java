package net.yvanchuxiuzhen.system.book.api;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** 由书籍所有者构造来源 textdisplay 阅读载体。 */
public final class BookTexts {
    private BookTexts() {
    }

    public static ItemStack create(Item carrier, String name, String text) {
        ItemStack stack = new ItemStack(carrier);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putString("textdisplay", text));
        return stack;
    }
}
