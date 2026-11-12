package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 技能菜单请求妖化；装备系统检查体质、冷却和背包并负责防具替换及生命周期。 */
public final class DemonTransformationEvent extends Event {
    private final ServerPlayer player;

    public DemonTransformationEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
