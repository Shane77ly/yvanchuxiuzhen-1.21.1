package net.yvanchuxiuzhen.system.book.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.book.BookBindingRuntime;

/** 装订台保留三十六个结果位与两个输入位的来源顺序；预览取出后由服务端扣料。 */
public final class BookBindingMenu extends AbstractContainerMenu {
    private final IItemHandler internal;
    private final BookBindingBlockEntity table;

    public BookBindingMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public BookBindingMenu(int id, Inventory inventory, BlockPos pos) {
        super(BookMenus.BINDING.get(), id);
        table = inventory.player.level().getBlockEntity(pos) instanceof BookBindingBlockEntity found ? found : null;
        internal = table == null ? new ItemStackHandler(38) : table.handler(null);
        for (int logical = 2; logical < 38; logical++) {
            int output = logical - 2;
            addSlot(new SlotItemHandler(internal, logical, 7 + output % 9 * 18, 9 + output / 9 * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    super.onTake(player, stack);
                    if (player instanceof ServerPlayer) BookBindingRuntime.take(BookBindingMenu.this);
                }
            });
        }
        addSlot(new SlotItemHandler(internal, 0, 133, 99) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.PAPER);
            }
        });
        addSlot(new SlotItemHandler(internal, 1, 151, 99) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.STRING);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 122 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 180));
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new BookBindingMenu(id, inventory, pos),
                Component.translatable("block.yvanchuxiuzhen.makebook")), buffer -> buffer.writeBlockPos(pos));
    }

    /** 来源自定义槽号与原生菜单序号不同：结果位在前，纸和线在最后。 */
    public Slot customSlot(int logical) {
        return slots.get(logical < 2 ? logical + 36 : logical - 2);
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
        if (index < 38) {
            if (!moveItemStackTo(stack, 38, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 38, false)) {
            if (index < 65) moveItemStackTo(stack, 65, slots.size(), true);
            else moveItemStackTo(stack, 38, 65, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源合并已有堆叠时也检查 mayPlace，快捷移动保持纸、线及只取结果位的过滤。 */
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

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer) BookBindingRuntime.clear(this);
        if (table == null && player instanceof ServerPlayer serverPlayer) {
            for (int slot = 0; slot < internal.getSlots(); slot++) {
                ItemStack stack = internal.extractItem(slot, internal.getStackInSlot(slot).getCount(), false);
                if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) player.drop(stack, false);
                else player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}
