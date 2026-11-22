package net.yvanchuxiuzhen.system.relation.api;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * 好友行刷新时在 {@code NeoForge.EVENT_BUS} 上同步查询已匹配在线玩家的境界名。
 * 修炼系统回填其保存的 {@code LEVELname}，关系系统仅消费文本；空字符串也是有效结果。
 * 没有监听者回填时读取结果抛出异常，表示必要的系统装配缺失。
 */
public final class FriendRealmNameEvent extends Event {
    private final ServerPlayer player;
    private Optional<String> realmName = Optional.empty();

    public FriendRealmNameEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }

    public String realmName() {
        return realmName.orElseThrow(() -> new IllegalStateException("好友境界查询缺少修炼系统响应"));
    }

    public void setRealmName(String realmName) {
        this.realmName = Optional.of(realmName);
    }
}
