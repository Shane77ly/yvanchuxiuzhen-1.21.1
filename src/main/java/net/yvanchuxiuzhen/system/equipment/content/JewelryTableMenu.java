package net.yvanchuxiuzhen.system.equipment.content;

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
import net.yvanchuxiuzhen.system.equipment.JewelryCrafting;

/** 饰品台四槽菜单；服务端绑定实际方块库存，客户端由菜单同步槽位，类别从对应方块实体快照读取。 */
public final class JewelryTableMenu extends AbstractContainerMenu {
    private final Container container;
    private final BlockPos pos;
    private final Level level;

    public JewelryTableMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(4), buffer.readBlockPos());
    }

    public JewelryTableMenu(int id, Inventory inventory, JewelryTableBlockEntity table) {
        this(id, inventory, table, table.getBlockPos());
    }

    private JewelryTableMenu(int id, Inventory inventory, Container container, BlockPos pos) {
        super(EquipmentMenus.JEWELRY_TABLE.get(), id);
        this.container = container;
        this.pos = pos;
        level = inventory.player.level();
        for (int index = 0; index < 4; index++) {
            final int slot = index;
            addSlot(new Slot(container, index, index == 3 ? 115 : 43 + 18 * index, 66) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return slot != 3;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    public BlockPos pos() {
        return pos;
    }

    public String category() {
        return level.getBlockEntity(pos) instanceof JewelryTableBlockEntity table ? table.category() : "";
    }

    /** 按钮只操作发送者当前绑定的方块实体，拆除、换台或超出容器距离后不再执行。 */
    public void press(ServerPlayer player, int button) {
        if (!(level.getBlockEntity(pos) instanceof JewelryTableBlockEntity table) || table != container || !stillValid(player)) return;
        switch (button) {
            case 0 -> JewelryCrafting.craft(table);
            case 1 -> table.cycleCategory(true);
            case 2 -> table.cycleCategory(false);
            default -> JewelryCrafting.hints(player, button - 2);
        }
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, slots.size(), true)) return ItemStack.EMPTY;
            slot.onQuickCraft(stack, original);
        } else if (!moveItemStackTo(stack, 0, 4, false)) {
            if (index < 31) moveItemStackTo(stack, 31, slots.size(), true);
            else moveItemStackTo(stack, 4, 31, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
