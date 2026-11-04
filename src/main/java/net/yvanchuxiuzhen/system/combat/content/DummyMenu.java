package net.yvanchuxiuzhen.system.combat.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/**
 * 木人桩设置界面的菜单（来源 {@code MurenzhuangGuiMenu}）：四格装备槽（头、胸、腿、脚，坐标 7/25/43/61, 10，不限制物品）
 * 与玩家物品栏。打开时装入木人桩当前的装备，并把木人桩的三项防御基值抄进玩家体质的 {@code attack_Def}、{@code mana_Def}、
 * {@code soul_Def}（界面标签据此显示）；关闭时把四格装备穿回木人桩，不退还玩家。菜单记下木人桩的实体编号，按钮在服务端据此找到它。
 */
public final class DummyMenu extends AbstractContainerMenu {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String[] DEFENSE_KEYS = {"attack_Def", "mana_Def", "soul_Def"};
    private final Container equipment;
    private final int dummyId;

    public DummyMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, new SimpleContainer(4), buffer.readVarInt());
    }

    public DummyMenu(int id, Inventory inventory, Container equipment, int dummyId) {
        super(CombatMenus.DUMMY.get(), id);
        this.equipment = equipment;
        this.dummyId = dummyId;
        for (int slot = 0; slot < 4; slot++) {
            addSlot(new Slot(equipment, slot, 7 + slot * 18, 10));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 141 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 199));
        }
    }

    /** 潜行右键木人桩：抄入防御基值、装入当前装备并打开界面。 */
    public static void open(Player player, WoodenDummyEntity dummy) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        for (String key : DEFENSE_KEYS) {
            Cultivation.setTizhiDouble(serverPlayer, key, dummy.getPersistentData().getDouble(key));
        }
        SimpleContainer equipment = new SimpleContainer(4);
        for (int slot = 0; slot < 4; slot++) {
            ItemStack worn = dummy.getItemBySlot(ARMOR[slot]).copy();
            if (!worn.isEmpty()) {
                worn.setCount(1);
            }
            equipment.setItem(slot, worn);
        }
        serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new DummyMenu(id, inventory, equipment, dummy.getId()),
                Component.literal("MurenzhuangGui")), buffer -> buffer.writeVarInt(dummy.getId()));
    }

    /** 按钮 0、1、2 设置防御基值（同时写进玩家体质供标签显示），3 拿走木人桩。 */
    public void press(ServerPlayer player, int button, String text) {
        if (!(player.level().getEntity(dummyId) instanceof WoodenDummyEntity dummy) || button < 0 || button > 3) {
            return;
        }
        if (button == 3) {
            takeAway(player, dummy);
            return;
        }
        double value = number(text);
        dummy.getPersistentData().putDouble(DEFENSE_KEYS[button], value);
        Cultivation.setTizhiDouble(player, DEFENSE_KEYS[button], value);
    }

    private static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /** 拿走木人桩：退回装备槽物品，移除木人桩并给玩家一个木人桩物品。 */
    private void takeAway(ServerPlayer player, WoodenDummyEntity dummy) {
        for (int slot = 0; slot < 4; slot++) {
            ItemStack stack = equipment.getItem(slot);
            if (!stack.isEmpty()) {
                ItemEntity drop = new ItemEntity(player.level(), player.getX(), player.getY() + 1, player.getZ(), stack);
                drop.setPickUpDelay(10);
                player.level().addFreshEntity(drop);
            }
        }
        equipment.clearContent();
        dummy.discard();
        player.getInventory().placeItemBackInInventory(new ItemStack(WoodenDummyItem.item()));
        player.closeContainer();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide() && player.level().getEntity(dummyId) instanceof WoodenDummyEntity dummy) {
            for (int slot = 0; slot < 4; slot++) {
                dummy.setItemSlot(ARMOR[slot], equipment.getItem(slot));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < 4) {
            if (!moveItemStackTo(stack, 4, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 4, false)) {
            boolean moved = index < 31 ? moveItemStackTo(stack, 31, slots.size(), true) : moveItemStackTo(stack, 4, 31, false);
            if (!moved) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
