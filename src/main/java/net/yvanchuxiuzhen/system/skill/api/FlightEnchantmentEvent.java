package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 飞行每秒扣费前查询四个饰品槽的 flying 附魔等级之和，由 equipment 提交；四件护甲由 skill 自行读取。 */
public final class FlightEnchantmentEvent extends Event {
    private final ServerPlayer player;
    private int levels;

    public FlightEnchantmentEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }

    public void add(int amount) {
        levels += amount;
    }

    public int levels() {
        return levels;
    }
}
