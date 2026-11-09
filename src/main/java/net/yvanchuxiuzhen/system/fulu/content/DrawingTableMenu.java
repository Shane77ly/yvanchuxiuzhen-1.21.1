package net.yvanchuxiuzhen.system.fulu.content;

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
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.fulu.DrawingTableRuntime;

/** 三槽画符菜单绑定实际装置；纸墨任意放取，成品可取不可放，选择属于共享方块。 */
public final class DrawingTableMenu extends AbstractContainerMenu {
    private final IItemHandler internal;
    private final DrawingTableBlockEntity table;
    private final Level level;
    private final BlockPos pos;

    public DrawingTableMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public DrawingTableMenu(int id, Inventory inventory, BlockPos pos) {
        super(FuluMenus.DRAWING_TABLE.get(), id);
        this.pos = pos.immutable();
        level = inventory.player.level();
        table = level.getBlockEntity(pos) instanceof DrawingTableBlockEntity found ? found : null;
        internal = table == null ? new ItemStackHandler(3) : table.handler(null);
        addSlot(new SlotItemHandler(internal, 0, 313, 247));
        addSlot(new SlotItemHandler(internal, 1, 349, 247));
        addSlot(new SlotItemHandler(internal, 2, 313, 175) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 242 + column * 18, 283 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 242 + column * 18, 341));
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new DrawingTableMenu(id, inventory, pos),
                Component.translatable("block.yvanchuxiuzhen.huafutai")), buffer -> buffer.writeBlockPos(pos));
    }

    public DrawingTableBlockEntity table() {
        return level.getBlockEntity(pos) instanceof DrawingTableBlockEntity current ? current : null;
    }

    public void press(ServerPlayer player, int button) {
        if (table == null || level.getBlockEntity(pos) != table) return;
        DrawingTableRuntime.button(table, player, button);
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
        if (index < 3) {
            if (!moveItemStackTo(stack, 3, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 3, false)) {
            if (index < 30) moveItemStackTo(stack, 30, slots.size(), true);
            else moveItemStackTo(stack, 3, 30, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源合并现有堆叠时仍检查 mayPlace，Shift 不能向成品叠入物品。 */
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
        if (table == null && player instanceof ServerPlayer serverPlayer) {
            for (int slot = 0; slot < internal.getSlots(); slot++) {
                ItemStack stack = internal.extractItem(slot, internal.getStackInSlot(slot).getCount(), false);
                if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) player.drop(stack, false);
                else player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}
