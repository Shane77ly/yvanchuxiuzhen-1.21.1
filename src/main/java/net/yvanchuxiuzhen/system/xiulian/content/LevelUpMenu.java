package net.yvanchuxiuzhen.system.xiulian.content;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** 升级界面的菜单，没有槽位；显示内容来自本人修炼附件中的升级文字（来源 {@code tizhiitem.uplevelxianshi1}–{@code 10}）。 */
public final class LevelUpMenu extends AbstractContainerMenu {
    public LevelUpMenu(int id, Inventory inventory) {
        super(XiulianMenus.LEVEL_UP.get(), id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
