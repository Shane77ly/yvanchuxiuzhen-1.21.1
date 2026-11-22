package net.yvanchuxiuzhen.system.economy.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 拍卖请求装饰内容族操作来源座椅；请求不持有装饰实体或其数据。 */
public final class AuctionSeatRequestEvent extends Event {
    public enum Operation { CREATE, RIDE, REMOVE, REMOVE_ALL }
    private final ServerLevel level;
    private final ServerPlayer player;
    private final BlockPos pos;
    private final Operation operation;

    public AuctionSeatRequestEvent(ServerLevel level, ServerPlayer player, BlockPos pos, Operation operation) {
        this.level = level;
        this.player = player;
        this.pos = pos.immutable();
        this.operation = operation;
    }

    public ServerLevel level() { return level; }
    public ServerPlayer player() { return player; }
    public BlockPos pos() { return pos; }
    public Operation operation() { return operation; }
}
