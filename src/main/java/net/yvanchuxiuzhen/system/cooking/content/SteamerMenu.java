package net.yvanchuxiuzhen.system.cooking.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.SteamerRuntime;

/** 蒸锅十四槽与笼屉九槽菜单绑定各自库存，蒸锅四个盛装槽按身份过滤。 */
public final class SteamerMenu extends AbstractContainerMenu {
    private static final String[] CONTAINERS = {"foodwan", "foodcipan", "fooddawan", "foodtiepan"};
    private final Container container;
    private final BlockPos pos;
    private final Level level;
    private final boolean tray;

    public SteamerMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer, boolean tray) {
        this(id, inventory, new SimpleContainer(tray ? 9 : 14) {
            @Override
            public int getMaxStackSize() { return tray ? 1 : 64; }
            @Override
            public int getMaxStackSize(ItemStack stack) { return tray ? 1 : 64; }
        }, buffer.readBlockPos(), tray);
    }

    public SteamerMenu(int id, Inventory inventory, SteamerBlockEntity pot) {
        this(id, inventory, pot, pot.getBlockPos(), pot.tray());
    }

    private SteamerMenu(int id, Inventory inventory, Container container, BlockPos pos, boolean tray) {
        super(tray ? SteamerMenus.TRAY.get() : SteamerMenus.POT.get(), id);
        this.container = container;
        this.pos = pos;
        this.tray = tray;
        level = inventory.player.level();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(container, row * 3 + column, (tray ? 61 : 224) + column * 18, (tray ? 17 : 161) + row * 18));
            }
        }
        if (!tray) {
            for (int index = 0; index < 4; index++) {
                String identity = CONTAINERS[index];
                addSlot(new Slot(container, index + 9, 133, 255 + index * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.is(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, identity)));
                    }
                });
            }
            addSlot(new Slot(container, 13, 439, 193));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, (tray ? 8 : 170) + column * 18, (tray ? 84 : 251) + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, (tray ? 8 : 170) + column * 18, tray ? 142 : 309));
    }

    public boolean tray() {
        return tray;
    }

    public ItemStack recipe() {
        return tray ? ItemStack.EMPTY : container.getItem(13);
    }

    public void press(ServerPlayer player, int button) {
        if (tray || !(container instanceof SteamerBlockEntity pot) || level.getBlockEntity(pos) != pot) return;
        SteamerRuntime.button(player, pot, button);
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
        int machineSlots = tray ? 9 : 14;
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
            if (index < machineSlots + 27) moveItemStackTo(stack, machineSlots + 27, slots.size(), true);
            else moveItemStackTo(stack, machineSlots, machineSlots + 27, false);
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
