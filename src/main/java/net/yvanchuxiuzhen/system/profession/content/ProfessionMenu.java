package net.yvanchuxiuzhen.system.profession.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.profession.api.ProfessionPageEvent;

/** 来源 PlayerGUI05 的背包与导航，职业显示读取本人同步进度。 */
public final class ProfessionMenu extends AbstractContainerMenu {
    private final BlockPos pos;

    public ProfessionMenu(int id, Inventory inventory) {
        this(id, inventory, inventory.player.blockPosition().above());
    }

    public ProfessionMenu(int id, Inventory inventory, BlockPos pos) {
        super(ProfessionMenus.PAGE.get(), id);
        this.pos = pos.immutable();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || !player.level().hasChunkAt(pos)) return false;
        switch (button) {
            case 0 -> NeoForge.EVENT_BUS.post(new ProfessionPageEvent(serverPlayer, 1, pos));
            case 1 -> NeoForge.EVENT_BUS.post(new ProfessionPageEvent(serverPlayer, 2, pos));
            case 2 -> NeoForge.EVENT_BUS.post(new ProfessionPageEvent(serverPlayer, 3, pos));
            case 3 -> NeoForge.EVENT_BUS.post(new ProfessionPageEvent(serverPlayer, 4, pos));
            case 4 -> NeoForge.EVENT_BUS.post(new ProfessionPageEvent(serverPlayer, 6, pos));
            case 5 -> {
                for (String line : List.of("炼丹、炼器的等级会影响你的炼丹、炼器时间；",
                        "画符、厨艺的等级会限制你能做的符箓、佳肴等级。", "除此之外，此界面的大部分技能都不会影响游戏效果。")) {
                    serverPlayer.displayClientMessage(Component.literal(line), false);
                }
            }
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
        // 来源没有自定义槽，背包搬移后直接返回 EMPTY。
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
