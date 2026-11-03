package net.yvanchuxiuzhen.system.relation.content;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** 好友界面的菜单，没有槽位；界面内容来自本人的关系附件，按钮经 {@code RelationButtonPayload} 交给服务端。 */
public final class FriendMenu extends AbstractContainerMenu {
    public FriendMenu(int id, Inventory inventory) {
        super(RelationMenus.FRIEND.get(), id);
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
