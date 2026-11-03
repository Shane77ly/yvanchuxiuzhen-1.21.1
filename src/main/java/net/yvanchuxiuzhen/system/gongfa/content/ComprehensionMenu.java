package net.yvanchuxiuzhen.system.gongfa.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;

/**
 * 参悟界面的菜单（来源 {@code CanwutaiMenu}）：参悟台的物品槽（坐标 241, 187，只接受功法书）与玩家物品栏。
 * 服务端的槽位直接是参悟台；客户端与原版容器一样用一格临时容器，内容随菜单同步。打开时写入方块位置，
 * 按钮在服务端据此找到参悟台，界面据此读取模式与说明。
 */
public final class ComprehensionMenu extends AbstractContainerMenu {
    private final BlockPos pos;

    public ComprehensionMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(1), buffer.readBlockPos());
    }

    public ComprehensionMenu(int id, Inventory inventory, Container table, BlockPos pos) {
        super(GongfaBlockEntities.COMPREHENSION_MENU.get(), id);
        this.pos = pos;
        addSlot(new Slot(table, 0, 241, 187) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return Techniques.book(stack).isPresent();
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 293 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 170 + column * 18, 351));
        }
    }

    public BlockPos pos() {
        return pos;
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
}
