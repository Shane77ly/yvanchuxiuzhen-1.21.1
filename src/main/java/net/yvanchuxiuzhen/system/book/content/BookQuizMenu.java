package net.yvanchuxiuzhen.system.book.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.book.BookQuiz;

/** 来源无槽答题页；请求绑定当前容器与已加载的打开坐标，书籍始终取答满时的主手。 */
public final class BookQuizMenu extends AbstractContainerMenu {
    private final BlockPos pos;

    public BookQuizMenu(int id, BlockPos pos) {
        super(BookMenus.QUIZ.get(), id);
        this.pos = pos;
    }

    public BookQuizMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, buffer.readBlockPos());
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || button < 0 || button > 3 || !player.level().hasChunkAt(pos)) {
            return false;
        }
        BookQuiz.answer(serverPlayer, button + 1);
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
