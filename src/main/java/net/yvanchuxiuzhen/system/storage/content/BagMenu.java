package net.yvanchuxiuzhen.system.storage.content;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.yvanchuxiuzhen.platform.ModId;

/** 乾坤袋菜单即时写回物品组件；关闭时库存已经属于袋物品，不向玩家返还袋内物品。 */
public final class BagMenu extends AbstractContainerMenu {
    private static final TagKey<Item> STORAGE_ITEMS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chuwu"));
    private final BagKind kind;

    public BagMenu(int id, Inventory inventory, BagKind kind) {
        this(id, inventory, kind, new ItemStackHandler(kind.size()));
    }

    public BagMenu(int id, Inventory inventory, BagKind kind, ItemStack bag) {
        this(id, inventory, kind, new BagInventory(bag, kind));
    }

    private BagMenu(int id, Inventory inventory, BagKind kind, IItemHandlerModifiable items) {
        super(BagMenus.type(kind), id);
        this.kind = kind;
        for (int index = 0; index < kind.size(); index++) {
            addSlot(new ItemHandlerCopySlot(items, index, kind.slotX(index), kind.slotY(index)) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !stack.is(STORAGE_ITEMS);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, kind.inventoryX() + column * 18, kind.inventoryY() + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, kind.inventoryX() + column * 18, kind.inventoryY() + 58));
        }
    }

    public BagKind kind() {
        return kind;
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
        int size = kind.size();
        if (index < size) {
            if (!moveItemStackTo(stack, size, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, size, false)) {
            if (index < size + 27) {
                if (!moveItemStackTo(stack, size + 27, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, size, size + 27, false)) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源合并已有堆叠时也检查槽位过滤；组件槽必须显式写回修改后的副本。 */
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
                moved = true;
                break;
            }
        }
        return moved;
    }
}
