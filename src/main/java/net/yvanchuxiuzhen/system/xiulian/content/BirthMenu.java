package net.yvanchuxiuzhen.system.xiulian.content;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.xiulian.BirthSelection;

/** 出身界面没有槽位；关闭后在来源打开维度等待两刻，再处理初始奖励和体质。 */
public final class BirthMenu extends AbstractContainerMenu {
    private final Level sourceLevel;

    public BirthMenu(int id, Inventory inventory) {
        super(XiulianMenus.BIRTH.get(), id);
        sourceLevel = inventory.player.level();
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
        if (player instanceof ServerPlayer serverPlayer && sourceLevel instanceof ServerLevel serverLevel) {
            BirthSelection.close(serverPlayer, serverLevel);
        }
    }
}
