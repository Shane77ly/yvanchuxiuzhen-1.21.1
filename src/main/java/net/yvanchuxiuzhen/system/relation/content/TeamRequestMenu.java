package net.yvanchuxiuzhen.system.relation.content;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** 好友申请界面的菜单，没有槽位；申请者名字来自本人关系附件中的待处理申请，按钮经 {@code RelationButtonPayload} 交给服务端。 */
public final class TeamRequestMenu extends AbstractContainerMenu {
    public TeamRequestMenu(int id, Inventory inventory) {
        super(RelationMenus.TEAM_REQUEST.get(), id);
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
