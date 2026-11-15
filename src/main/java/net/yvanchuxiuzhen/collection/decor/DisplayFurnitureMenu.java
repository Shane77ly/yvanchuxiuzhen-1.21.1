package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** 展示菜单绑定家具无方向库存，保留单件槽位、切换按钮和未绑定关闭归还顺序。 */
public final class DisplayFurnitureMenu extends AbstractContainerMenu {
    private final boolean cabinet;
    private final BlockPos pos;
    private final int size;
    private final IItemHandler handler;
    private final boolean bound;

    public DisplayFurnitureMenu(boolean cabinet, int id, Inventory inventory, BlockPos pos) {
        super(cabinet ? DisplayFurnitureContent.CABINET.get() : DisplayFurnitureContent.STAND.get(), id);
        this.cabinet = cabinet;
        this.pos = pos;
        size = cabinet ? 3 : 1;
        IItemHandler storage = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        bound = storage != null;
        handler = bound ? storage : new ItemStackHandler(size);
        for (int index = 0; index < size; index++) {
            addSlot(new SlotItemHandler(handler, index, cabinet ? 52 + index * 27 : 79, cabinet ? 35 : 17));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public boolean cabinet() { return cabinet; }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (cabinet || button != 0) return false;
        if (player.level().hasChunkAt(pos)) DisplayFurnitureMenuActions.switchShape(player, pos);
        return true;
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
        if (index < size) {
            if (!moveItemStackTo(stack, size, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, size, false)) {
            if (index < size + 27) moveItemStackTo(stack, size + 27, slots.size(), true);
            else moveItemStackTo(stack, size, size + 27, false);
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
                slot.setChanged();
                moved = true;
                break;
            }
        }
        return moved;
    }

    /** 来源未绑定方块时使用临时库存，关闭时逐槽归还；已绑定家具不回写副本。 */
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!bound && player instanceof ServerPlayer serverPlayer) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stack = handler.extractItem(slot, handler.getStackInSlot(slot).getCount(), false);
                if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) player.drop(stack, false);
                else player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}
