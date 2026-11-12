package net.yvanchuxiuzhen.system.liandan.content;

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
import net.yvanchuxiuzhen.system.liandan.MedicineSeparation;

/** 分丹、合丹保留独立菜单身份与槽位布局，共用同一台面的三槽持久库存。 */
public final class MedicineSeparationMenu extends AbstractContainerMenu {
    private final IItemHandler internal;
    private final MedicineSeparationBlockEntity table;
    private final BlockPos pos;
    private final Level level;
    private final boolean merging;

    public MedicineSeparationMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer, boolean merging) {
        this(id, inventory, buffer.readBlockPos(), merging);
    }

    public MedicineSeparationMenu(int id, Inventory inventory, BlockPos pos, boolean merging) {
        super((merging ? LiandanMenus.MEDICINE_COMBINATION : LiandanMenus.MEDICINE_SEPARATION).get(), id);
        this.pos = pos;
        this.merging = merging;
        level = inventory.player.level();
        table = level.getBlockEntity(pos) instanceof MedicineSeparationBlockEntity found ? found : null;
        internal = table == null ? new ItemStackHandler(3) : table.handler(null);
        addSlot(new SlotItemHandler(internal, 0, 61, merging ? 17 : 53));
        addSlot(new SlotItemHandler(internal, 1, 97, merging ? 17 : 53));
        addSlot(new SlotItemHandler(internal, 2, 79, merging ? 53 : 17));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public static void open(ServerPlayer player, BlockPos pos, boolean merging) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new MedicineSeparationMenu(id, inventory, pos, merging),
                Component.literal(merging ? "HedanGUI" : "FendanGUI")), buffer -> buffer.writeBlockPos(pos));
    }

    public boolean merging() {
        return merging;
    }

    public void press(ServerPlayer player, int button) {
        if (table == null || level.getBlockEntity(pos) != table) return;
        if (button == 0) {
            open(player, pos, !merging);
        } else {
            if (merging) MedicineSeparation.merge(player, this);
            else MedicineSeparation.split(player, this);
            table.setChanged();
            broadcastChanges();
        }
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

    /** 来源合并已有堆叠时也执行槽位过滤，并显式写回物品能力。 */
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
