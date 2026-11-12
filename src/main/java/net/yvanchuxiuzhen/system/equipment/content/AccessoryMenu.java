package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.api.AccessoryPageEvent;
import net.yvanchuxiuzhen.system.equipment.data.AccessoryData;
import net.yvanchuxiuzhen.system.equipment.data.EquipmentData;

/** 饰品四槽首刻装入、关闭保存；槽2收手饰却存腰佩，槽3收腰佩却存手饰，保留来源 Q60。 */
public final class AccessoryMenu extends AbstractContainerMenu {
    private static final List<String> TAGS = List.of("headornaments", "necklaceornaments", "handornaments", "beltornaments");
    private final BlockPos pos;
    private boolean initialized;

    public AccessoryMenu(int id, Inventory inventory, BlockPos pos) {
        super(EquipmentMenus.ACCESSORIES.get(), id);
        this.pos = pos.immutable();
        IItemHandler bound = inventory.player.level().getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
        IItemHandler items = bound == null ? new ItemStackHandler(4) : bound;
        for (int index = 0; index < 4; index++) {
            int slot = index;
            TagKey<Item> tag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, TAGS.get(index)));
            addSlot(new SlotItemHandler(items, slot, 169, 163 + slot * 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.is(tag);
                }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    /** 仅在服务端玩家 Post tick 调用一次，不能提前到构造器而改变菜单开启时序。 */
    public void initialize(ServerPlayer player) {
        if (initialized) return;
        AccessoryData saved = player.getData(EquipmentData.ACCESSORIES);
        for (int index = 0; index < 4; index++) {
            ItemStack stack = saved.item(index);
            stack.setCount(1);
            slots.get(index).set(stack);
        }
        initialized = true;
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (button == 0 || button == 2) {
            NeoForge.EVENT_BUS.post(new AccessoryPageEvent(serverPlayer, button == 0 ? 1 : 4, pos));
            return true;
        }
        if (button == 6) {
            for (String line : List.of("左侧四个槽位，从上到下分别是头饰、项坠、腰佩和手饰。", "您可以用饰品台制作各类饰品。",
                    "右侧是您当前加入的临时队伍，临时队伍可以一同进入副本。", "点击“X”可以退出当前小队。")) {
                serverPlayer.displayClientMessage(Component.literal(line), false);
            }
            return true;
        }
        return false;
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
            if (index < 31) {
                if (!moveItemStackTo(stack, 31, slots.size(), true)) return ItemStack.EMPTY;
            } else if (!moveItemStackTo(stack, 4, 31, false)) {
                return ItemStack.EMPTY;
            }
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    /** 来源在合并已有堆叠时仍检查标签，避免重开载入的不匹配槽绕过 mayPlace。 */
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
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.setData(EquipmentData.ACCESSORIES, new AccessoryData(slots.get(0).getItem(), slots.get(1).getItem(),
                    slots.get(2).getItem(), slots.get(3).getItem()));
        }
    }
}
