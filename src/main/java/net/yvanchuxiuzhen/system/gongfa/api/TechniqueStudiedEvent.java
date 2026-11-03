package net.yvanchuxiuzhen.system.gongfa.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * 玩家研习功法成功后在 {@code NeoForge.EVENT_BUS} 上发出。来源研习“行尸御火”“真言吒雷诀”时经 {@code /jineng} 命令附赠技能，
 * 该部分由 skill 监听本事件按功法名处理。
 */
public final class TechniqueStudiedEvent extends Event {
    private final ServerPlayer player;
    private final String technique;

    public TechniqueStudiedEvent(ServerPlayer player, String technique) {
        this.player = player;
        this.technique = technique;
    }

    public ServerPlayer player() {
        return player;
    }

    public String technique() {
        return technique;
    }
}
