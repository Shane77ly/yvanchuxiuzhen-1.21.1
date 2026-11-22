package net.yvanchuxiuzhen.system.creature.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerCopySlot;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.SpiritBeastBagRuntime;
import net.yvanchuxiuzhen.system.creature.item.SpiritBeastDyeToolItem;

/** 三槽染色菜单绑定到开启时指定手持染色工具的容器组件。 */
public final class SpiritBeastBagDyeMenu extends AbstractContainerMenu {
    private static final TagKey<Item> BAGS = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "lingshoudai"));
    private static final TagKey<Item> DYES = ItemTags.create(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "ranliao"));
    private final BlockPos position;

    public SpiritBeastBagDyeMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), buffer.readByte() == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
    }

    public SpiritBeastBagDyeMenu(int id, Inventory inventory, BlockPos position, InteractionHand hand) {
        super(SpiritBeastBagDyeMenus.TYPE.get(), id);
        this.position = position;
        ItemStack tool = hand == InteractionHand.MAIN_HAND ? inventory.player.getMainHandItem() : inventory.player.getOffhandItem();
        SpiritBeastDyeToolInventory items = tool.getItem() instanceof SpiritBeastDyeToolItem
                ? new SpiritBeastDyeToolInventory(inventory.player.level().isClientSide() ? tool.copy() : tool) : null;
        IItemHandlerModifiable handler = items == null ? new ItemStackHandler(3) : items;
        addSlot(new ItemHandlerCopySlot(handler, 0, 34, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(BAGS); }
        });
        addSlot(new ItemHandlerCopySlot(handler, 1, 52, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(DYES); }
        });
        addSlot(new ItemHandlerCopySlot(handler, 2, 124, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public BlockPos position() { return position; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button != 0 || !(player instanceof ServerPlayer serverPlayer)
                || !stillValid(player) || !player.level().hasChunkAt(position)) return false;
        SpiritBeastBagRuntime.dye(serverPlayer, this);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 3) {
            if (!moveItemStackTo(stack, 3, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 3, false)) {
            if (index < 30) {
                if (!moveItemStackTo(stack, 30, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 3, 30, false)) return ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
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
                moved = true;
                break;
            }
        }
        return moved;
    }
}
