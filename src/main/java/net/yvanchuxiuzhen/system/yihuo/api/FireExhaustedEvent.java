package net.yvanchuxiuzhen.system.yihuo.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 燃料不足时查询上方炉鼎；装置所有者在 lianpower 至少为一时翻转 gengxin 并保留火块。 */
public final class FireExhaustedEvent extends Event {
    private final ServerLevel level;
    private final BlockPos pos;
    private boolean retained;

    public FireExhaustedEvent(ServerLevel level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    public ServerLevel level() { return level; }
    public BlockPos pos() { return pos; }
    public boolean retained() { return retained; }
    public void retain() { retained = true; }
}
