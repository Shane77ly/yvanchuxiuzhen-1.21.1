package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 出身菜单关闭后的第二个服务端结束刻，在体质生效前交接初始奖励和来源传送。 */
public final class BirthInitialRewardsEvent extends Event {
    private final ServerPlayer player;
    private final ServerLevel sourceLevel;

    public BirthInitialRewardsEvent(ServerPlayer player, ServerLevel sourceLevel) {
        this.player = player;
        this.sourceLevel = sourceLevel;
    }

    public ServerPlayer player() { return player; }
    public ServerLevel sourceLevel() { return sourceLevel; }
}
