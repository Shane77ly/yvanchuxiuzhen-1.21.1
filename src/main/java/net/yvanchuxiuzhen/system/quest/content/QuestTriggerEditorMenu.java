package net.yvanchuxiuzhen.system.quest.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.quest.QuestTriggerEditorRuntime;

/** 一个展示槽、玩家背包及十行脚本配置的服务端容器。 */
public final class QuestTriggerEditorMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    private final SimpleContainer unbound = new SimpleContainer(1);

    public QuestTriggerEditorMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public QuestTriggerEditorMenu(int id, Inventory inventory, BlockPos pos) {
        super(QuestTriggerEditorMenus.EDITOR.get(), id);
        this.pos = pos.immutable();
        DisguisedQuestBlockEntity block = block(inventory.player);
        addSlot(new Slot(block == null ? unbound : block, 0, 7, 8));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
    }

    public void apply(ServerPlayer player, String state) {
        if (player.serverLevel().hasChunkAt(pos)) QuestTriggerEditorRuntime.apply(player, block(player), state);
    }

    public boolean advanced(Player player, int row) {
        DisguisedQuestBlockEntity block = block(player);
        return block != null && !block.renwulist(row).isEmpty();
    }

    private DisguisedQuestBlockEntity block(Player player) {
        return player.level().getBlockEntity(pos) instanceof DisguisedQuestBlockEntity block ? block : null;
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
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            if (index < 28) {
                if (!moveItemStackTo(stack, 28, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 1, 28, false)) return ItemStack.EMPTY;
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
        if (player instanceof ServerPlayer) clearContainer(player, unbound);
    }
}
