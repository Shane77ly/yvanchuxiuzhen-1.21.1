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
import net.yvanchuxiuzhen.system.liandan.Pharmacology;

/** 药理台菜单露出媒介、八格药材和两个说明槽，预览物品留在隐藏槽。 */
public final class PharmacologyMenu extends AbstractContainerMenu {
    private final IItemHandler internal;
    private final PharmacologyBlockEntity table;
    private final BlockPos pos;
    private final Level level;
    private static volatile String inputName = "";

    public PharmacologyMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public PharmacologyMenu(int id, Inventory inventory, BlockPos pos) {
        super(LiandanMenus.PHARMACOLOGY.get(), id);
        this.pos = pos;
        level = inventory.player.level();
        table = level.getBlockEntity(pos) instanceof PharmacologyBlockEntity found ? found : null;
        internal = table == null ? new ItemStackHandler(11) : table.handler(null);
        addSlot(new SlotItemHandler(internal, 0, 79, 62));
        for (int slot = 1; slot <= 8; slot++) addSlot(new SlotItemHandler(internal, slot, 16 + (slot - 1) * 18, 17));
        addSlot(new SlotItemHandler(internal, 9, 151, -19));
        addSlot(new SlotItemHandler(internal, 10, 169, -19));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new PharmacologyMenu(id, inventory, pos),
                Component.literal("Yaolitai")), buffer -> buffer.writeBlockPos(pos));
    }

    /** 来源输入框只保存在客户端静态状态，集成端共享此值，专服保持空串自动命名。 */
    public static void inputName(String value) {
        inputName = value;
    }

    public PharmacologyBlockEntity table() {
        return table;
    }

    public void press(Player player, int button) {
        if (table == null || level.getBlockEntity(pos) != table) return;
        if (button == 0) Pharmacology.write(player, this, inputName);
        else Pharmacology.refresh(table, table, inputName);
        table.setChanged();
        if (player instanceof ServerPlayer) broadcastChanges();
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
        if (index < 11) {
            if (!moveItemStackTo(stack, 11, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 11, false)) {
            if (index < 38) moveItemStackTo(stack, 38, slots.size(), true);
            else moveItemStackTo(stack, 11, 38, false);
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
