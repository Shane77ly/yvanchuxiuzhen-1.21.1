package net.yvanchuxiuzhen.system.npc;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 天道NPC设置菜单：六个设备槽及玩家背包，编辑文本由设置提交载荷写回手持工具。 */
public final class NpcManagementMenu extends AbstractContainerMenu {
    private final InteractionHand hand;
    private final SimpleContainer equipment;
    private final ItemStack tool;

    public NpcManagementMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, readHand(buffer));
    }

    private NpcManagementMenu(int id, Inventory inventory, InteractionHand hand) {
        this(id, inventory, hand, readEquipment(inventory.player, hand));
    }

    public NpcManagementMenu(int id, Inventory inventory, InteractionHand hand, SimpleContainer equipment) {
        super(NpcManagementMenus.MENU.get(), id);
        this.hand = hand;
        this.equipment = equipment;
        this.tool = inventory.player.getItemInHand(hand);
        for (int slot = 0; slot < 6; slot++) addSlot(new Slot(equipment, slot, 156 + slot * 18, 149));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 129 + column * 18, 171 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 129 + column * 18, 229));
    }

    public InteractionHand hand() { return hand; }

    public String fieldValue(String key) {
        return tool.getOrDefault(NpcData.TOOL_PROFILE.get(), NpcToolProfile.EMPTY).fieldValue(key);
    }

    public boolean hasBoundTool() {
        return !tool.isEmpty() && tool.getItem() instanceof NpcManagementItem;
    }

    public void saveProfile(List<String> values, double level, boolean fixed) {
        NpcToolProfile current = tool.getOrDefault(NpcData.TOOL_PROFILE.get(), NpcToolProfile.EMPTY);
        tool.set(NpcData.TOOL_PROFILE.get(), current.withEditedFields(values, level, fixed));
    }

    private static InteractionHand readHand(RegistryFriendlyByteBuf buffer) {
        return switch (buffer.readUnsignedByte()) {
            case 0 -> InteractionHand.MAIN_HAND;
            case 1 -> InteractionHand.OFF_HAND;
            default -> throw new IllegalArgumentException("Invalid NPC management hand");
        };
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer && hasBoundTool()) saveEquipment(serverPlayer);
    }

    public static SimpleContainer readEquipment(Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        NpcToolProfile profile = tool.getOrDefault(NpcData.TOOL_PROFILE.get(), NpcToolProfile.EMPTY);
        SimpleContainer container = new SimpleContainer(6);
        for (int index = 0; index < container.getContainerSize(); index++) {
            container.setItem(index, profile.equipment().get(index).copy());
        }
        return container;
    }

    public void saveEquipment(ServerPlayer player) {
        NpcToolProfile current = tool.getOrDefault(NpcData.TOOL_PROFILE.get(), NpcToolProfile.EMPTY);
        List<ItemStack> items = new ArrayList<>(equipment.getContainerSize());
        for (int slot = 0; slot < equipment.getContainerSize(); slot++) {
            items.add(equipment.getItem(slot).copy());
        }
        tool.set(NpcData.TOOL_PROFILE.get(), current.withEquipment(items));
    }

    @Override
    public boolean stillValid(Player player) { return hasBoundTool(); }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 6) {
            if (!moveItemStackTo(stack, 6, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 0, 6, false)) {
            if (index < 33) moveItemStackTo(stack, 33, slots.size(), true);
            else moveItemStackTo(stack, 6, 33, false);
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }
}
