package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * 闭关修炼每过一天、吸纳天地元气后在 {@code NeoForge.EVENT_BUS} 上发出（来源 {@code Kaishixiulian} 在此处给主修功法加经验）。
 * {@code experience} 为本次吸纳的元气（不含额外修为）；gongfa 据此给主修功法加经验。
 */
public final class CultivationExperienceEvent extends Event {
    private final ServerPlayer player;
    private final double experience;

    public CultivationExperienceEvent(ServerPlayer player, double experience) {
        this.player = player;
        this.experience = experience;
    }

    public ServerPlayer player() {
        return player;
    }

    public double experience() {
        return experience;
    }
}
