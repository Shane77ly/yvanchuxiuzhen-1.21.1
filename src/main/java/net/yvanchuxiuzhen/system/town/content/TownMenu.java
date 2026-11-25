package net.yvanchuxiuzhen.system.town.content;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.platform.content.SeamlessMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 城镇页面不携带可编辑玩家档案；占领与官职按钮在来源没有服务端动作。 */
public final class TownMenu extends AbstractContainerMenu {
    public final BlockPos pos;
    public final int page;

    public TownMenu(int id, Inventory inventory, BlockPos pos, int page) {
        super(TownMenus.page(page), id);
        this.pos = pos;
        this.page = page;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column + (row + 1) * 9, 8 + column * 18, 84 + row * 18));
        }
        for (int column = 0; column < 9; column++) addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        if (page == 3 && inventory.player instanceof ServerPlayer && inventory.player.level().getBlockEntity(pos) instanceof TownBlockEntity town) {
            CompoundTag build = town.townBuild();
            if (!build.getString("build_list").isEmpty()) {
                for (String entry : build.getString("build_list").split("/")) {
                    String type = build.getString(entry + "_type").replace("txt.yvanchuxitong.build", "txt.yvanchuxiuzhen.build");
                    build.putInt(type, build.getInt(type) + 1);
                }
            }
            town.setTownBuild(build);
            town.sync();
        }
    }

    public static void open(ServerPlayer player, BlockPos pos, int page) {
        player.openMenu(new SeamlessMenuProvider((id, inventory, opener) -> new TownMenu(id, inventory, pos, page),
                Component.literal("城镇")), buffer -> buffer.writeBlockPos(pos));
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot.hasItem()) moveItemStackTo(slot.getItem(), index < 27 ? 27 : 0, index < 27 ? 36 : 27, index < 27);
        return ItemStack.EMPTY;
    }
}
