package net.yvanchuxiuzhen.system.cooking.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.yvanchuxiuzhen.system.cooking.WoodenStillRuntime;

/** 木甑四个原料槽及只取的结果槽，快捷移动保留来源独立扫描规则。 */
public final class WoodenStillMenu extends AbstractContainerMenu {
    private final WineVesselBlockEntity still;
    private final BlockPos pos;
    private final Level level;

    public WoodenStillMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, mirror(), buffer.readBlockPos(), null);
    }

    public WoodenStillMenu(int id, Inventory inventory, WineVesselBlockEntity still) {
        this(id, inventory, still.handler(null), still.getBlockPos(), still);
    }

    private WoodenStillMenu(int id, Inventory inventory, IItemHandler handler, BlockPos pos, WineVesselBlockEntity still) {
        super(WineMenus.WOODEN_STILL.get(), id);
        this.still = still;
        this.pos = pos;
        level = inventory.player.level();
        addSlot(new SlotItemHandler(handler, 0, 41, 17));
        addSlot(new SlotItemHandler(handler, 1, 59, 17));
        addSlot(new SlotItemHandler(handler, 2, 41, 35));
        addSlot(new SlotItemHandler(handler, 3, 59, 35));
        addSlot(new SlotItemHandler(handler, 4, 114, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    private static IItemHandler mirror() {
        Container container = new SimpleContainer(5) {
            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 64;
            }
        };
        return new InvWrapper(container);
    }

    public BlockPos pos() {
        return pos;
    }

    public double progress() {
        return level.getBlockEntity(pos) instanceof WineVesselBlockEntity vessel ? vessel.getPersistentData().getDouble("jindu") : 0;
    }

    public void press(ServerPlayer player, int button) {
        if (still == null || level.getBlockEntity(pos) != still) return;
        WoodenStillRuntime.button(player, still, button);
        broadcastChanges();
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
        if (index < 5) {
            if (!moveItemStackTo(stack, 5, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 5, false)) {
            if (index < 32) moveItemStackTo(stack, 32, slots.size(), true);
            else moveItemStackTo(stack, 5, 32, false);
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
}
