package net.yvanchuxiuzhen.panel;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.xiulian.api.Dantian;

/** 属性汇总页只含原生背包槽、分页和丹田入口，来源 PlayerGUI01 的 36 格布局。 */
public final class PlayerPanelMenu extends AbstractContainerMenu {
    public PlayerPanelMenu(int id, Inventory inventory) {
        super(PanelNavigation.ATTRIBUTES.get(), id);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        switch (button) {
            case 1 -> PanelNavigation.openAccessories(serverPlayer);
            case 5 -> {
                for (String line : List.of("右侧是您当前的灵根等级。", "境界下方依次是：", "剩余血量/血量上限（血量恢复速度）",
                        "剩余灵力/灵力上限（灵力恢复速度）释放法术技能时的能量消耗。", "剩余精力/精力上限（精力恢复速度）释放精神秘法时的能量消耗。",
                        "物理攻击/物理防御", "法术攻击/法术防御", "精神攻击/精神防御", "罡气/煞气 功德/业力")) {
                    serverPlayer.displayClientMessage(Component.literal(line), false);
                }
            }
            case 6 -> Dantian.open(serverPlayer);
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        if (index < 27) moveItemStackTo(stack, 27, 36, true);
        else moveItemStackTo(stack, 0, 27, false);
        // 来源无自定义槽的分支搬移后直接返回 EMPTY。
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
