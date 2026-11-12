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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.system.liandan.LiandanHerbAnalysis;

/** 百草鉴显示玩家解析缓存；样本槽优先绑定打开坐标的方块库存，否则关闭时返还临时物品。 */
public final class HerbAnalysisMenu extends AbstractContainerMenu {
    private final IItemHandler input;
    private final boolean bound;
    private final BlockPos openedAt;

    public HerbAnalysisMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public HerbAnalysisMenu(int id, Inventory inventory, BlockPos openedAt) {
        super(LiandanMenus.HERB_ANALYSIS.get(), id);
        this.openedAt = openedAt;
        Player owner = inventory.player;
        IItemHandler blockItems = owner.level().isClientSide ? null
                : owner.level().getCapability(Capabilities.ItemHandler.BLOCK, openedAt, null);
        bound = blockItems != null;
        input = bound ? blockItems : new ItemStackHandler(1) {
            @Override
            public int getSlotLimit(int slot) {
                return 64;
            }
        };
        addSlot(new SlotItemHandler(input, 0, 30, -13) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(LiandanHerbAnalysis.HERBS);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public static void open(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new HerbAnalysisMenu(id, inventory, pos),
                Component.literal("Baicaojian")), pos);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || !player.level().hasChunkAt(openedAt)) return false;
        switch (button) {
            case 0 -> LiandanHerbAnalysis.refresh(serverPlayer, this);
            case 1 -> LiandanHerbAnalysis.identify(serverPlayer, this);
            default -> { return false; }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            if (index < 28) {
                if (!moveItemStackTo(stack, 28, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 1, 28, false)) return ItemStack.EMPTY;
            return ItemStack.EMPTY;
        }
        slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源合并已有堆叠时仍检查槽位过滤，空槽插入一次后停止。 */
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
        if (!bound && player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = input.extractItem(0, input.getStackInSlot(0).getCount(), false);
            if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) player.drop(stack, false);
            else player.getInventory().placeItemBackInInventory(stack);
        }
    }
}
