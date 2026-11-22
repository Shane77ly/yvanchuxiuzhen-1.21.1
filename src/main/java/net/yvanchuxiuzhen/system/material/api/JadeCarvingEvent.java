package net.yvanchuxiuzhen.system.material.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/** 璞玉物品使用时交由跨系统流程执行雕琢。 */
public final class JadeCarvingEvent extends Event {
    private final ServerPlayer player;
    private final ItemStack input;
    private final int number;

    public JadeCarvingEvent(ServerPlayer player, ItemStack input, int number) {
        this.player = player;
        this.input = input;
        this.number = number;
    }

    public ServerPlayer player() { return player; }
    public ItemStack input() { return input; }
    public int number() { return number; }
}
