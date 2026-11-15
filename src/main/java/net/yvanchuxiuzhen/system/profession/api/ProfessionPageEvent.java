package net.yvanchuxiuzhen.system.profession.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 副职业页的跨页请求由面板监听，职业系统不依赖面板。 */
public final class ProfessionPageEvent extends Event {
    private final ServerPlayer player;
    private final int page;
    private final BlockPos pos;

    public ProfessionPageEvent(ServerPlayer player, int page, BlockPos pos) {
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

    /** 当前菜单坐标，打开下一页时按来源再向上移一格。 */
    public BlockPos pos() {
        return pos;
    }
}
