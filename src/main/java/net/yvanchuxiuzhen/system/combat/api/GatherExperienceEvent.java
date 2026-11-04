package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * 连锁采集（附魔 {@code chain}）挖到矿石时在 {@code NeoForge.EVENT_BUS} 上发出（来源执行命令
 * {@code collection experience add <数量>} 给玩家的采集职业加经验，职业规则归 profession）。
 */
public final class GatherExperienceEvent extends Event {
    private final ServerPlayer player;
    private final double experience;

    public GatherExperienceEvent(ServerPlayer player, double experience) {
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
