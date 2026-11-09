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
import net.yvanchuxiuzhen.system.cooking.CookingRuntime;

/** 两锅菜单绑定真实库存，仅四个盛装槽限制放入物，移动顺序保留来源十四槽扫描。 */
public final class CookingPotMenu extends AbstractContainerMenu {
    private static final String[] CONTAINERS = {"foodwan", "foodcipan", "fooddawan", "foodtiepan"};
    private final Container container;
    private final BlockPos pos;
    private final Level level;
    private final boolean iron;

    public CookingPotMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer, boolean iron) {
        this(id, inventory, new SimpleContainer(14), buffer.readBlockPos(), iron);
    }

    public CookingPotMenu(int id, Inventory inventory, CookingPotBlockEntity pot) {
        this(id, inventory, pot, pot.getBlockPos(), pot.iron());
    }

    private CookingPotMenu(int id, Inventory inventory, Container container, BlockPos pos, boolean iron) {
        super(iron ? CookingMenus.IRON.get() : CookingMenus.CLAY.get(), id);
        this.container = container;
        this.pos = pos;
        this.iron = iron;
        level = inventory.player.level();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(container, row * 3 + column, 224 + column * 18, (iron ? 143 : 161) + row * 18));
            }
        }
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
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    public boolean iron() {
        return iron;
    }

    public ItemStack recipe() {
        return container.getItem(13);
    }

    public void press(ServerPlayer player, int button) {
        if (!(container instanceof CookingPotBlockEntity pot) || level.getBlockEntity(pos) != pot) return;
        CookingRuntime.button(player, pot, button);
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
        if (index < 14) {
            if (!moveItemStackTo(stack, 14, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 14, false)) {
            if (index < 41) moveItemStackTo(stack, 41, slots.size(), true);
            else moveItemStackTo(stack, 14, 41, false);
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
