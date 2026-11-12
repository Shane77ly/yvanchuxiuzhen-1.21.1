package net.yvanchuxiuzhen.system.guchong.content;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** 三档蛊罐菜单：开启重建临时虫槽，关闭时按来源写入当前主手蛊罐。 */
public final class TaoguanMenu extends AbstractContainerMenu {
    private final int grade;
    private final int size;

    public TaoguanMenu(int id, Inventory inventory, int grade, BlockPos position) {
        super(GuchongMenus.type(grade), id);
        this.grade = grade;
        size = TaoguanItem.capacity(grade);
        IItemHandler contents = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, position, null);
        if (contents == null) contents = new ItemStackHandler(size);
        for (int slot = 0; slot < size; slot++) {
            int x = grade == 1 ? 79 : grade == 2 ? 70 + slot % 2 * 18 : 61 + slot % 3 * 18;
            int y = grade == 1 ? 35 : grade == 2 ? 26 + slot / 2 * 18 : 17 + slot / 3 * 18;
            addSlot(new SlotItemHandler(contents, slot, x, y) {
                @Override public boolean mayPlace(ItemStack stack) { return stack.is(TaoguanItem.INSECTS); }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        if (!inventory.player.level().isClientSide()) {
            CompoundTag jar = GuchongJarData.read(inventory.player.getMainHandItem());
            for (int slot = 0; slot < TaoguanItem.capacity(TaoguanItem.grade(inventory.player.getMainHandItem())); slot++) {
                ItemStack insect = GuchongJarData.unpack(jar, slot, true);
                if (!insect.isEmpty()) slots.get(slot).set(insect);
            }
        }
    }

    public int grade() { return grade; }

    @Override public boolean stillValid(Player player) { return true; }

    @Override public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide()) return;
        ItemStack jar = player.getMainHandItem();
        int capacity = TaoguanItem.capacity(TaoguanItem.grade(jar));
        if (capacity == 0) return;
        CompoundTag data = GuchongJarData.read(jar);
        for (int slot = 0; slot < capacity; slot++) GuchongJarData.pack(slots.get(slot).getItem(), data, slot, false);
        GuchongJarData.write(jar, data);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
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
            } else if (!moveItemStackTo(stack, size, size + 27, false)) return ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override protected boolean moveItemStackTo(ItemStack stack, int start, int end, boolean reverse) {
        boolean moved = false;
        int step = reverse ? -1 : 1;
        if (stack.isStackable()) {
            for (int index = reverse ? end - 1 : start; !stack.isEmpty() && index >= start && index < end; index += step) {
                Slot slot = slots.get(index);
                ItemStack existing = slot.getItem();
                if (!slot.mayPlace(existing) || existing.isEmpty() || !ItemStack.isSameItemSameComponents(stack, existing)) continue;
                int amount = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), stack.getMaxStackSize()) - existing.getCount());
                if (amount > 0) {
                    stack.shrink(amount);
                    existing.grow(amount);
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
