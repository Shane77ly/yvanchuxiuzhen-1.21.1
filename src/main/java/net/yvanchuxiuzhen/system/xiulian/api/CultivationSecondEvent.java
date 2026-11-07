package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * 修炼每秒推进时在 {@code NeoForge.EVENT_BUS} 上发出，位置在闭关修炼之后、吸收修为之前（来源 {@code PlayerTick} 在此处按
 * {@code tizhiitem.xiuliankaiguan} 为“功法”时调用 {@code Kaishigongfa}，随后调用出窍耗神）。gongfa 据此推进功法修炼，skill 推进元婴出窍耗神。
 */
public final class CultivationSecondEvent extends Event {
    private final ServerPlayer player;

    public CultivationSecondEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
