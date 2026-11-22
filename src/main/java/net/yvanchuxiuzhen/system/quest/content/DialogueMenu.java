package net.yvanchuxiuzhen.system.quest.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.quest.DialogueRuntime;

/** 服务端现读会话后处理继续或选择，容器不接收客户端提交的脚本和物品。 */
public final class DialogueMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final boolean choices;

    public DialogueMenu(int id, Inventory inventory, BlockPos pos, boolean choices) {
        super(choices ? DialogueMenus.CHOICES.get() : DialogueMenus.TEXT.get(), id);
        this.pos = pos;
        this.choices = choices;
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
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (choices && button >= 0 && button < 4) return DialogueRuntime.choose(serverPlayer, button + 1, pos);
        if (!choices && button == 0) {
            DialogueRuntime.next(serverPlayer, pos);
            return true;
        }
        return false;
    }
}
