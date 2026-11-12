package net.yvanchuxiuzhen.system.lianqi.content;

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
import net.yvanchuxiuzhen.system.lianqi.BlueprintTableRuntime;

/** 图纸设计菜单绑定二百二十三个真实库存槽，材料目录禁止放入并允许取出。 */
public final class BlueprintTableMenu extends AbstractContainerMenu {
    private final IItemHandler internal;
    private final BlueprintTableBlockEntity table;
    private final BlockPos pos;
    private final Level level;
    private static volatile String inputName = "";

    public BlueprintTableMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public BlueprintTableMenu(int id, Inventory inventory, BlockPos pos) {
        super(LianqiMenus.BLUEPRINT_TABLE.get(), id);
        this.pos = pos;
        level = inventory.player.level();
        table = level.getBlockEntity(pos) instanceof BlueprintTableBlockEntity found ? found : null;
        internal = table == null ? new ItemStackHandler(223) : table.handler(null);
        for (int slot = 0; slot < 9; slot++) addSlot(new SlotItemHandler(internal, slot, 294 + slot * 18, 146));
        addSlot(new SlotItemHandler(internal, 9, 375, 164));
        addSlot(new SlotItemHandler(internal, 10, 294, 164));
        addSlot(new SlotItemHandler(internal, 11, 312, 164));
        addSlot(new SlotItemHandler(internal, 12, 438, 164));
        for (int slot = 13; slot < 223; slot++) {
            addSlot(new SlotItemHandler(internal, slot, 15 + (slot - 13) % 15 * 18, 11 + (slot - 13) / 15 * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 295 + column * 18, 187 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 295 + column * 18, 245));
        if (table != null && !level.isClientSide()) BlueprintTableRuntime.populate(table);

    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new BlueprintTableMenu(id, inventory, pos),
                Component.translatable("block.yvanchuxiuzhen.tiandaotuzhitai")), buffer -> buffer.writeBlockPos(pos));
    }

    /** 来源输入框只保存在客户端静态状态，集成端共享此值，专服保持空串自动命名。 */
    public static void inputName(String value) {
        inputName = value;
    }

    public BlueprintTableBlockEntity table() {
        return table;
    }

    public void press(Player player, int button) {
        if (table == null || level.getBlockEntity(pos) != table) return;
        if (button == 0) BlueprintTableRuntime.refresh(table);
        else BlueprintTableRuntime.save(table, inputName);
        table.changed();
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
        if (index < 223) {
            if (!moveItemStackTo(stack, 223, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 223, false)) {
            if (index < 250) moveItemStackTo(stack, 250, slots.size(), true);
            else moveItemStackTo(stack, 223, 250, false);
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
                if (slot >= 13 && slot != 45) continue;
                ItemStack stack = internal.extractItem(slot, internal.getStackInSlot(slot).getCount(), false);
                if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) player.drop(stack, false);
                else player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}
