package net.yvanchuxiuzhen.system.npc.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** 来源无槽菜单，仅承载方块坐标和一个循环任务按钮。 */
public final class QiankunmentuMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public QiankunmentuMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) { this(id, inventory, buffer.readBlockPos()); }
    public QiankunmentuMenu(int id, Inventory inventory, BlockPos pos) { super(QiankunMenus.DISCIPLE.get(), id); this.pos = pos; }
    @Override public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer) || button != 0 || !(player.level().getBlockEntity(pos) instanceof QiankunmentuBlockEntity entity)) return false;
        entity.incrementRenwuset();
        return true;
    }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
