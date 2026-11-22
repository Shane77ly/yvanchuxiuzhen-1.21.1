package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.yvanchuxiuzhen.collection.decor.FurnitureStorageVariant.Layout;

/** 家具菜单直接绑定无方向库存；来源不按距离关闭菜单，Shift 顺序保留自定义槽与背包之间的转移规则。 */
public final class FurnitureStorageMenu extends AbstractContainerMenu {
    private final Layout layout;
    private final IItemHandler handler;
    private final boolean bound;

    public FurnitureStorageMenu(Layout layout, int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(layout, id, inventory, buffer.readBlockPos());
    }

    public FurnitureStorageMenu(Layout layout, int id, Inventory inventory, BlockPos pos) {
        super(FurnitureStorageContent.menuType(layout), id);
        this.layout = layout;
        IItemHandler storage = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        bound = storage != null;
        handler = bound ? storage : new ItemStackHandler(layout.slots());
        for (int index = 0; index < layout.slots(); index++) {
            addSlot(new SlotItemHandler(handler, index, 7 + index % 9 * 18, layout.storageY() + index / 9 * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, layout.inventoryY() + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, layout.hotbarY()));
    }

    public Layout layout() {
        return layout;
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
        int size = layout.slots();
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
