package net.yvanchuxiuzhen.system.economy.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.Event;

/** 请求NPC所有者的世界定义快照，本场竞价状态随后归拍卖装置所有。 */
public final class AuctionNpcRequestEvent extends Event {
    private final ServerLevel level;
    private CompoundTag definitions = new CompoundTag();

    public AuctionNpcRequestEvent(ServerLevel level) { this.level = level; }
    public ServerLevel level() { return level; }
    public void setDefinitions(CompoundTag definitions) { this.definitions = definitions.copy(); }
    public CompoundTag definitions() { return definitions.copy(); }
}
