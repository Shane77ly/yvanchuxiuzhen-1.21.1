package net.yvanchuxiuzhen.platform.content;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;

/** 切换界面时不先向客户端发送关闭容器包，避免鼠标被 {@code grabMouse/releaseMouse} 重置到屏幕中心。 */
public final class SeamlessMenuProvider implements MenuProvider {
    private final MenuConstructor constructor;
    private final Component title;

    public SeamlessMenuProvider(MenuConstructor constructor, Component title) {
        this.constructor = constructor;
        this.title = title;
    }

    @Override
    public Component getDisplayName() {
        return title;
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return constructor.createMenu(containerId, inventory, player);
    }

    @Override
    public boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return false;
    }
}
