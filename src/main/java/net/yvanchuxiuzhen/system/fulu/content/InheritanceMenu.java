package net.yvanchuxiuzhen.system.fulu.content;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.system.fulu.InheritanceRuntime;

/** 传承设置菜单没有库存槽，按钮只操作当前菜单绑定的方块实体。 */
public final class InheritanceMenu extends AbstractContainerMenu {
    private final BlockPos pos;
    private final Level level;
    private final InheritanceBlockEntity inheritance;

    public InheritanceMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(id, inventory, buffer.readBlockPos(), null);
    }

    public InheritanceMenu(int id, Inventory inventory, InheritanceBlockEntity inheritance) {
        this(id, inventory, inheritance.getBlockPos(), inheritance);
    }

    private InheritanceMenu(int id, Inventory inventory, BlockPos pos, InheritanceBlockEntity inheritance) {
        super(InheritanceContent.MENU.get(), id);
        this.pos = pos;
        level = inventory.player.level();
        this.inheritance = inheritance;
    }

    public Component selection() {
        int head = level.getBlockEntity(pos) instanceof InheritanceBlockEntity current
                ? current.getPersistentData().getInt("weizhi") : 0;
        return head == 0 ? Component.literal("选择大修士")
                : Component.translatable(String.format(Locale.ROOT, "txt.yvanchuxiuzhen.huafu_futou_%02d", head));
    }

    public void press(ServerPlayer player) {
        if (inheritance != null && level.getBlockEntity(pos) == inheritance) InheritanceRuntime.cycle(inheritance);
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
