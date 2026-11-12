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
import net.yvanchuxiuzhen.system.lianqi.SewingBasket;

/** 八个只读预览槽绑定真实笸箩库存；页码属于装置，关闭菜单清空该装置的预览。 */
public final class SewingBasketMenu extends AbstractContainerMenu {
    private final SewingBasketBlockEntity basket;
    private final BlockPos pos;
    private final Level level;

    public SewingBasketMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public SewingBasketMenu(int id, Inventory inventory, BlockPos pos) {
        super(LianqiMenus.SEWING_BASKET.get(), id);
        this.pos = pos.immutable();
        level = inventory.player.level();
        basket = level.getBlockEntity(pos) instanceof SewingBasketBlockEntity found ? found : null;
        IItemHandler items = basket == null ? new ItemStackHandler(8) : basket.handler(null);
        for (int index = 0; index < 8; index++) {
            addSlot(new SlotItemHandler(items, index, index < 4 ? 52 + index * 18 : 97 + (index - 4) * 18, index < 4 ? 17 : 62) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        if (inventory.player instanceof ServerPlayer serverPlayer) SewingBasket.opened(serverPlayer, this.pos);
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new SewingBasketMenu(id, inventory, pos),
                Component.translatable("block.yvanchuxiuzhen.zhenxianpoluo")), pos);
    }

    public BlockPos pos() {
        return pos;
    }

    public void setPreview(int slot, ItemStack stack) {
        getSlot(slot).set(stack);
        broadcastChanges();
    }

    public void press(ServerPlayer player, int button) {
        if (basket == null || level.getBlockEntity(pos) != basket) return;
        if (button == 2) SewingBasket.craft(player, this);
        else SewingBasket.page(player, basket, button == 1);
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
        if (index < 8) {
            if (!moveItemStackTo(stack, 8, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 8, false)) {
            if (index < 35) moveItemStackTo(stack, 35, slots.size(), true);
            else moveItemStackTo(stack, 8, 35, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源合并已有堆叠时仍检查 mayPlace，Shift 不能向材料需求预览叠入物品。 */
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
        SewingBasket.closed(level, pos);
    }
}
