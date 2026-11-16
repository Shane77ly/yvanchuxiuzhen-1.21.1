package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** 两来源匾额菜单及牌位菜单均不带库存，编辑请求只写绑定位置的文字装饰。 */
public final class InscribedDecorMenu extends AbstractContainerMenu {
    public enum Kind {
        PLAQUE("plaque_gui"), LEGACY_PLAQUE("plaque_gui_xitong"), MEMORIAL("paiwei_gui");
        private final String id;
        Kind(String id) { this.id = id; }
        public String id() { return id; }
        public boolean memorial() { return this == MEMORIAL; }
    }
    private final Kind kind;
    private final BlockPos pos;
    private final Level level;

    public InscribedDecorMenu(Kind kind, int id, Inventory inventory, BlockPos pos) {
        super(InscribedDecorContent.menu(kind), id);
        this.kind = kind;
        this.pos = pos.immutable();
        level = inventory.player.level();
    }
    public Kind kind() { return kind; }
    public InscribedDecorBlockEntity block() { return level.getBlockEntity(pos) instanceof InscribedDecorBlockEntity block ? block : null; }
    @Override public boolean stillValid(Player player) { return true; }
    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    public void apply(ServerPlayer player, int button, String serialized) {
        if (!level.hasChunkAt(pos)) return;
        InscribedDecorBlockEntity block = block();
        if (block == null || block.variant().memorial() != kind.memorial()) return;
        if (kind.memorial()) {
            if (button == 0) {
                InscribedDecor.setMemorial(block, InscribedDecor.sourceText(serialized));
                player.closeContainer();
            }
        } else {
            if (button == 0 && kind == Kind.PLAQUE) InscribedDecor.setPlaque(block, player, InscribedDecor.sourceText(serialized));
            if (button == 1) InscribedDecor.cycleAngle(block);
            if (button == 2) InscribedDecor.toggleHalf(block);
        }
    }
}
