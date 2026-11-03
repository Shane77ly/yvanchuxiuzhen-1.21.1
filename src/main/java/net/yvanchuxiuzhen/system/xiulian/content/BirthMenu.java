package net.yvanchuxiuzhen.system.xiulian.content;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;

/** 出身界面的菜单，没有槽位；显示内容来自本人的修炼附件，关闭时在服务端让体质生效。 */
public final class BirthMenu extends AbstractContainerMenu {
    public BirthMenu(int id, Inventory inventory) {
        super(XiulianMenus.BIRTH.get(), id);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            BirthSelection.close(serverPlayer);
        }
    }
}
