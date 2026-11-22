package net.yvanchuxiuzhen.system.npc;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

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
        CompoundTag data = tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        return key.equals("npclevel") ? Double.toString(data.getDouble(key)) : data.getString(key);
    }

    public boolean hasBoundTool() {
        return !tool.isEmpty() && tool.getItem() instanceof NpcManagementItem;
    }

    public void saveProfile(List<String> values, double level, boolean fixed) {
        CompoundTag data = tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        data.putDouble("npclevel", level);
        for (int index = 0; index < values.size(); index++) {
            String key = NpcManagementData.KEYS.get(index);
            if (!key.equals("npclevel")) data.putString(key, values.get(index));
        }
        if (fixed) data.putBoolean("npcset", true);
        tool.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
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
        CompoundTag item = tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ListTag list = item.getCompound("Inventory").getList("Items", Tag.TAG_COMPOUND);
        SimpleContainer container = new SimpleContainer(6);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            int slot = entry.getByte("Slot") & 255;
            if (slot < container.getContainerSize()) container.setItem(slot, ItemStack.parseOptional(player.registryAccess(), entry));
        }
        return container;
    }

    public void saveEquipment(ServerPlayer player) {
        CompoundTag data = tool.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        CompoundTag inventory = new CompoundTag();
        ListTag items = new ListTag();
        for (int slot = 0; slot < equipment.getContainerSize(); slot++) {
            ItemStack stack = equipment.getItem(slot);
            if (stack.isEmpty()) continue;
            CompoundTag entry = (CompoundTag) stack.save(player.registryAccess());
            entry.putByte("Slot", (byte) slot);
            items.add(entry);
        }
        inventory.putInt("Size", equipment.getContainerSize());
        inventory.put("Items", items);
        data.put("Inventory", inventory);
        tool.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
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
