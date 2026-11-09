package net.yvanchuxiuzhen.system.lianqi.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.lianqi.RepairTableRuntime;

/** 修理菜单直接绑定真实单槽，开窗只填空槽提示，槽变化不自动刷新材料需求。 */
public final class RepairTableMenu extends AbstractContainerMenu {
    private final Container container;
    private final BlockPos pos;
    private final Level level;

    public RepairTableMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(1), buffer.readBlockPos());
    }

    public RepairTableMenu(int id, Inventory inventory, RepairTableBlockEntity table) {
        this(id, inventory, table, table.getBlockPos());
        RepairTableRuntime.opened(table);
    }

    private RepairTableMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(LianqiMenus.REPAIR_TABLE.get(), id);
        this.container = container;
        this.pos = pos;
        level = inventory.player.level();
        addSlot(new Slot(container, 0, 79, -26));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public BlockPos pos() {
        return pos;
    }

    public String requirement() {
        return level.getBlockEntity(pos) instanceof RepairTableBlockEntity table
                ? table.getPersistentData().getString("needcailiao") : "";
    }

    /** 服务端只操作当前菜单绑定且仍存在的同一台实体。 */
    public void press(ServerPlayer player, int button) {
        if (!(container instanceof RepairTableBlockEntity table) || level.getBlockEntity(pos) != table) return;
        RepairTableRuntime.button(player, table, button);
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            if (index < 28) moveItemStackTo(stack, 28, slots.size(), true);
            else moveItemStackTo(stack, 1, 28, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean moved = false;
        int step = reverse ? -1 : 1;
        if (stack.isStackable()) {
            for (int index = reverse ? end - 1 : start; !stack.isEmpty() && index >= start && index < end; index += step) {
                Slot slot = slots.get(index);
                ItemStack existing = slot.getItem();
                if (!slot.mayPlace(existing) || existing.isEmpty() || !ItemStack.isSameItemSameComponents(stack, existing)) continue;
                int count = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), stack.getMaxStackSize()) - existing.getCount());
                if (count > 0) {
                    stack.shrink(count);
                    existing.grow(count);
                    slot.set(existing);
                    moved = true;
                }
            }
        }
        for (int index = reverse ? end - 1 : start; !stack.isEmpty() && index >= start && index < end; index += step) {
            Slot slot = slots.get(index);
            if (slot.getItem().isEmpty() && slot.mayPlace(stack)) {
                slot.set(stack.split(Math.min(stack.getCount(), slot.getMaxStackSize())));
                moved = true;
                break;
            }
        }
        return moved;
    }
}
