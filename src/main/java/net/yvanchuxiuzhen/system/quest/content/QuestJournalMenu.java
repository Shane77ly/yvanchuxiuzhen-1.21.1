package net.yvanchuxiuzhen.system.quest.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.quest.QuestJournalRuntime;

/** 任务列表没有物品槽，按钮只提交操作编号，任务内容取自服务端本人目录。 */
public final class QuestJournalMenu extends AbstractContainerMenu {
    public final BlockPos pos;

    public QuestJournalMenu(int id, Inventory inventory, BlockPos pos) {
        super(DialogueMenus.JOURNAL.get(), id);
        this.pos = pos;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        return player instanceof ServerPlayer serverPlayer && serverPlayer.serverLevel().hasChunkAt(pos)
                && QuestJournalRuntime.click(serverPlayer, button);
    }
}
