package net.yvanchuxiuzhen.system.economy.content;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** 银行两页共用的来源库存与归还生命周期；附带槽映射供来源延后操作现读目标。 */
abstract class BankInventoryMenu extends AbstractContainerMenu implements Supplier<Map<Integer, Slot>> {
    protected final IItemHandler items;
    protected final BlockPos pos;
    private final int size;
    private final int[] returnSlots;
    private final boolean bound;
    private final Map<Integer, Slot> customSlots = new LinkedHashMap<>();

    protected BankInventoryMenu(MenuType<?> type, int id, Inventory inventory, BlockPos pos, int size, int... returnSlots) {
        super(type, id);
        this.pos = pos;
        this.size = size;
        this.returnSlots = returnSlots;
        IItemHandler blockInventory = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        bound = blockInventory != null;
        items = bound ? blockInventory : new ItemStackHandler(size);
    }

    protected void addCustom(Slot slot) {
        customSlots.put(customSlots.size(), addSlot(slot));
    }

    protected void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 267 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 170 + column * 18, 325));
        }
    }

    @Override
    public Map<Integer, Slot> get() {
        return Map.copyOf(customSlots);
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
            if (index < size + 27) {
                if (!moveItemStackTo(stack, size + 27, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, size, size + 27, false)) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源合并已有栈时仍核对mayPlace，防止Shift把货币合入禁放的展示槽。 */
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (bound || !(player instanceof ServerPlayer serverPlayer)) return;
        for (int index : returnSlots) {
            ItemStack stack = items.extractItem(index, items.getStackInSlot(index).getCount(), false);
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                player.drop(stack, false);
            } else {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}
