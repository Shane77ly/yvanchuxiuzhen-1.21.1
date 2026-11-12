package net.yvanchuxiuzhen.system.craft.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.yvanchuxiuzhen.system.craft.PotteryRuntime;

/** 一个共享结果槽和八个只读目录槽；按钮只操作当前绑定的陶轮。 */
public final class PotteryWheelMenu extends AbstractContainerMenu {
    private final Container container;
    private final BlockPos pos;
    private final Level level;

    public PotteryWheelMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, clientContainer(), buffer.readBlockPos());
    }

    public PotteryWheelMenu(int id, Inventory inventory, PotteryWheelBlockEntity table) {
        this(id, inventory, table, table.getBlockPos());
    }

    private PotteryWheelMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(CraftMenus.POTTERY_WHEEL.get(), id);
        this.container = container;
        this.pos = pos;
        level = inventory.player.level();
        IItemHandler handler = container instanceof PotteryWheelBlockEntity table ? table.handler(null) : new InvWrapper(container);
        for (int index = 0; index < 9; index++) {
            int x = index == 0 ? 79 : 16 + (index - 1) * 18;
            int y = index == 0 ? 8 : 53;
            final int slotIndex = index;
            addSlot(new SlotItemHandler(handler, index, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return slotIndex == 0;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    private static Container clientContainer() {
        return new SimpleContainer(9) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public int getMaxStackSize(ItemStack stack) {
                return 64;
            }
        };
    }

    public BlockPos pos() {
        return pos;
    }

    public Component outputName() {
        if (!(level.getBlockEntity(pos) instanceof PotteryWheelBlockEntity wheel)
                || wheel.getPersistentData().getString("wenben1").isEmpty()) return Component.empty();
        int selected = wheel.getPersistentData().getInt("xvanzeweizhi");
        String itemId = wheel.getPersistentData().getString("chengpinid" + selected);
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId))).getHoverName();
    }

    public Component costLabel() {
        if (!(level.getBlockEntity(pos) instanceof PotteryWheelBlockEntity wheel)
                || wheel.getPersistentData().getString("wenben2").isEmpty()) return Component.empty();
        return Component.translatable("gui.yvanchuxiuzhen.craft.pottery_clay");
    }

    public Component costAmount() {
        if (!(level.getBlockEntity(pos) instanceof PotteryWheelBlockEntity wheel)
                || wheel.getPersistentData().getString("wenben3").isEmpty()) return Component.empty();
        int selected = wheel.getPersistentData().getInt("xvanzeweizhi");
        return Component.translatable("gui.yvanchuxiuzhen.craft.pottery_count",
                wheel.getPersistentData().getInt("needitem" + selected));
    }

    public boolean press(ServerPlayer player, int button) {
        if (player.containerMenu != this || button < 0 || button > 10 || !stillValid(player)
                || !(level.getBlockEntity(pos) instanceof PotteryWheelBlockEntity table) || table != container) return false;
        if (button <= 1) PotteryRuntime.changePage(table, button == 1);
        else if (button <= 9) PotteryRuntime.select(table, button - 2);
        else PotteryRuntime.craft(table, player);
        broadcastChanges();
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        return player instanceof ServerPlayer sender && press(sender, button);
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
        if (index < 9) {
            if (!moveItemStackTo(stack, 9, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 9, false)) {
            if (index < 36) moveItemStackTo(stack, 36, slots.size(), true);
            else moveItemStackTo(stack, 9, 36, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!level.isClientSide && container instanceof PotteryWheelBlockEntity wheel) wheel.clearPreviews();
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
