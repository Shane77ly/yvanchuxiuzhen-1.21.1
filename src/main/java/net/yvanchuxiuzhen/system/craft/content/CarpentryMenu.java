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
import net.yvanchuxiuzhen.system.craft.CarpentryRuntime;

/** 四十展示槽、一个配方预览和一个共享结果槽；菜单按钮只操作当前绑定的木匠台。 */
public final class CarpentryMenu extends AbstractContainerMenu {
    private final Container container;
    private final BlockPos pos;
    private final Level level;

    public CarpentryMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, clientContainer(), buffer.readBlockPos());
    }

    public CarpentryMenu(int id, Inventory inventory, CarpentryBlockEntity table) {
        this(id, inventory, table, table.getBlockPos());
    }

    private CarpentryMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(CraftMenus.CARPENTRY.get(), id);
        this.container = container;
        this.pos = pos;
        level = inventory.player.level();
        IItemHandler handler = container instanceof CarpentryBlockEntity table ? table.handler(null) : new InvWrapper(container);
        for (int index = 0; index < 42; index++) {
            int x = index < 40 ? -100 + index % 5 * 18 : index == 40 ? 25 : 79;
            int y = index < 40 ? -24 + index / 5 * 18 : index == 40 ? 8 : -55;
            final int slotIndex = index;
            addSlot(new SlotItemHandler(handler, index, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return slotIndex == 41;
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
        return new SimpleContainer(42) {
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

    public String category() {
        return level.getBlockEntity(pos) instanceof CarpentryBlockEntity table ? table.category() : "";
    }

    public int selected() {
        return level.getBlockEntity(pos) instanceof CarpentryBlockEntity table ? table.selected() : 0;
    }

    public Component outputName() {
        ItemStack preview = container.getItem(40);
        return preview.isEmpty() ? Component.empty() : preview.getDisplayName();
    }

    /** 显示名在客户端按物品翻译生成，需求标识和数量仍读取服务端方块实体快照。 */
    public Component requirementName(int row) {
        if (!(level.getBlockEntity(pos) instanceof CarpentryBlockEntity table)) return Component.empty();
        String itemId = table.requirementId(row);
        if (itemId.isEmpty()) return Component.empty();
        ItemStack material = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId)));
        return material.getDisplayName().copy().append("：" + table.requirementCount(row));
    }

    public boolean press(ServerPlayer player, int button) {
        if (player.containerMenu != this || button < 0 || button > 42 || !stillValid(player)
                || !(level.getBlockEntity(pos) instanceof CarpentryBlockEntity table) || table != container) return false;
        if (button == 0) CarpentryRuntime.craft(table, player);
        else if (button <= 40) CarpentryRuntime.select(table, button - 1);
        else CarpentryRuntime.cycleCategory(table);
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
        if (index < 42) {
            if (!moveItemStackTo(stack, 42, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 42, false)) {
            if (index < 69) moveItemStackTo(stack, 69, slots.size(), true);
            else moveItemStackTo(stack, 42, 69, false);
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
