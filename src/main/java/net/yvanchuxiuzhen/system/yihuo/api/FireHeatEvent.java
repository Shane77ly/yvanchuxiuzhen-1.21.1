package net.yvanchuxiuzhen.system.yihuo.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 火块每二十刻消耗燃料后向上方装置提交热量；装置所有者将 amount 累加到自身 lianpower。 */
public final class FireHeatEvent extends Event {
    private final ServerLevel level;
    private final BlockPos pos;
    private final double amount;

    public FireHeatEvent(ServerLevel level, BlockPos pos, double amount) {
        this.level = level;
        this.pos = pos;
        this.amount = amount;
    }

    public ServerLevel level() { return level; }
    public BlockPos pos() { return pos; }
    public double amount() { return amount; }
}
