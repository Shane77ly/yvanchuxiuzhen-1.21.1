package net.yvanchuxiuzhen.system.book.content;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** 来源阅读页没有物品槽与按钮，关闭时不持有或返还任何库存。 */
public final class BookReadingMenu extends AbstractContainerMenu {
    public BookReadingMenu(int id) {
        super(BookMenus.READING.get(), id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
