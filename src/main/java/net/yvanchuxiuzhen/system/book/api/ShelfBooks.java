package net.yvanchuxiuzhen.system.book.api;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.treasure.api.TreasureContainers;

/** 初始奖励使用的书架藏宝书，由书籍所有者构造实际自然右键入口读取的实例。 */
public final class ShelfBooks {
    private ShelfBooks() {
    }

    public static ItemStack technique(double level) {
        return create("一本功法书", "功法书", level);
    }

    public static ItemStack skill(double level) {
        return create("一本技能书", "技能书", level);
    }

    private static ItemStack create(String name, String type, double level) {
        ItemStack stack = TreasureContainers.create(
                BuiltInRegistries.ITEM.get(ResourceLocation.parse("yvanchuxiuzhen:book_021")), "书架", type, level, 0, 0);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }
}
