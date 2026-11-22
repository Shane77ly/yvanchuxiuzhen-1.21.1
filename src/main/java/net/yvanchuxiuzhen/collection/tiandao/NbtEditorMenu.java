package net.yvanchuxiuzhen.collection.tiandao;

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

/** 编辑请求只操作当前菜单的一号物品槽；来源菜单不按玩家距离关闭。 */
public final class NbtEditorMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final IItemHandler handler;
    private final boolean bound;

    public NbtEditorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public NbtEditorMenu(int id, Inventory inventory, BlockPos pos) {
        super(NbtEditorSetup.MENU.get(), id);
        this.pos = pos.immutable();
        IItemHandler storage = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        bound = storage != null;
        handler = bound ? storage : new ItemStackHandler(1);
        addSlot(new SlotItemHandler(handler, 0, 140, 27));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public void apply(ServerPlayer player, String state) {
        if (!player.serverLevel().hasChunkAt(pos)) return;
        if (bound && !(player.level().getBlockEntity(pos) instanceof NbtEditorBlockEntity)) return;
        NbtEditorRuntime.apply(player, getSlot(0), state);
        broadcastChanges();
    }

    @Override public boolean stillValid(Player player) { return true; }

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
        if (!bound && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = handler.extractItem(0, handler.getStackInSlot(0).getCount(), false);
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) player.drop(stack, false);
            else player.getInventory().placeItemBackInInventory(stack);
        }
    }
}
