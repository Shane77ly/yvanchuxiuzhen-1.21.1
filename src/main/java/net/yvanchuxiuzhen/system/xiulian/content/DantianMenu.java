package net.yvanchuxiuzhen.system.xiulian.content;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.xiulian.DantianActions;

/**
 * 丹田界面的菜单（来源 {@code DantianGuiMenu}）：本命槽 1 格（坐标 141, 100，只接受 {@link DantianActions#canHold} 的物品，
 * 来源的槽位上限为 64）与玩家物品栏。本命槽不是物品容器：打开时放入本命法宝，关闭时槽内物品写回本命槽，不退还给玩家。
 */
public final class DantianMenu extends AbstractContainerMenu {
    private final Container benming;

    public DantianMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1));
    }

    public DantianMenu(int id, Inventory inventory, Container benming) {
        super(XiulianMenus.DANTIAN.get(), id);
        this.benming = benming;
        addSlot(new Slot(benming, 0, 141, 100) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DantianActions.canHold(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 70 + column * 18, 186 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 70 + column * 18, 244));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            boolean moved = index < 28 ? moveItemStackTo(stack, 28, slots.size(), true) : moveItemStackTo(stack, 1, 28, false);
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            DantianActions.close(serverPlayer, benming.getItem(0));
        }
    }
}
