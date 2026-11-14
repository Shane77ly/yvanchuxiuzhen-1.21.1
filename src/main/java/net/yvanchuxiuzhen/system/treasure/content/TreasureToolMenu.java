package net.yvanchuxiuzhen.system.treasure.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.treasure.TreasureToolSettings;

/** 十个按钮操作当前主手，只保留来源打开坐标的已加载区块条件。 */
public final class TreasureToolMenu extends AbstractContainerMenu {
    private final BlockPos position;

    public TreasureToolMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos());
    }

    public TreasureToolMenu(int id, Inventory inventory, BlockPos position) {
        super(TreasureMenus.TOOL.get(), id);
        this.position = position;
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        return player.level().hasChunkAt(position) && TreasureToolSettings.action(player.getMainHandItem(), button);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
