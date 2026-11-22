package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 修炼三页的外部导航请求，面板负责记录主分页并打开目标系统页面。 */
public final class RetreatPageEvent extends Event {
    private final ServerPlayer player;
    private final int page;
    private final BlockPos pos;

    public RetreatPageEvent(ServerPlayer player, int page, BlockPos pos) {
        this.player = player;
        this.page = page;
        this.pos = pos.immutable();
    }

    public ServerPlayer player() {
        return player;
    }

    public int page() {
        return page;
    }

    public BlockPos pos() {
        return pos;
    }
}
