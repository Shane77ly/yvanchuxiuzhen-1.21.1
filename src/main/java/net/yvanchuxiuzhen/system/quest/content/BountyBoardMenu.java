package net.yvanchuxiuzhen.system.quest.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.quest.BountyRuntime;

/** 六个目录按钮直接领取对应悬赏；服务端只使用当前告示牌状态。 */
public final class BountyBoardMenu extends AbstractContainerMenu {
    public final BlockPos pos;

    public BountyBoardMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public BountyBoardMenu(int id, Inventory inventory, BlockPos pos) {
        super(DialogueMenus.BOUNTY.get(), id);
        this.pos = pos;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 170 + column * 18, 251 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 170 + column * 18, 309));
    }

    public static void open(ServerPlayer player, BlockPos pos) {
        player.openMenu(new SimpleMenuProvider((id, inventory, opener) -> new BountyBoardMenu(id, inventory, pos),
                Component.translatable("block.yvanchuxiuzhen.xvanshang")), buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(player.level().getBlockEntity(pos) instanceof BountyBoardBlockEntity board)) return false;
        if (button >= 0 && button < 6) BountyRuntime.track(serverPlayer, board, button + 1);
        else if (button == 6) BountyRuntime.information(serverPlayer);
        else return false;
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        if (index < 27) moveItemStackTo(stack, 27, 36, true);
        else moveItemStackTo(stack, 0, 27, false);
        return ItemStack.EMPTY;
    }
}
