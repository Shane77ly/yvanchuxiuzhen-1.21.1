package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 属性加点页的跨页请求，由汇总导航打开目标页。 */
public final class SkillPointPageEvent extends Event {
    private final ServerPlayer player;
    private final int page;
    private final BlockPos pos;

    public SkillPointPageEvent(ServerPlayer player, int page, BlockPos pos) {
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

    /** 当前菜单打开坐标，导航按来源向上移一格后打开下一页。 */
    public BlockPos pos() {
        return pos;
    }
}
