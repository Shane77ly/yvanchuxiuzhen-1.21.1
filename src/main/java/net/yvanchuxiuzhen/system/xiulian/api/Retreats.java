package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.xiulian.RetreatActions;

/** 修炼主分页的公开打开入口，子页切换保持同一主分页记忆。 */
public final class Retreats {
    private Retreats() {
    }

    public static void openPage(ServerPlayer player, BlockPos pos) {
        RetreatActions.open(player, pos, 6);
    }
}
