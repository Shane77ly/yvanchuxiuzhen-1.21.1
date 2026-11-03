package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.xiulian.DantianActions;

/** 丹田界面的公开操作：由玩家面板的丹田按钮打开（来源 {@code Dakaidantiangui}）。 */
public final class Dantian {
    private Dantian() {
    }

    public static void open(ServerPlayer player) {
        DantianActions.open(player);
    }
}
