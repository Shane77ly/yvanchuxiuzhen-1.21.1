package net.yvanchuxiuzhen.system.equipment.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 饰品页请求切换属性或装配页，由汇总导航监听。 */
public final class AccessoryPageEvent extends Event {
    private final ServerPlayer player;
    private final int page;
    private final BlockPos pos;

    public AccessoryPageEvent(ServerPlayer player, int page) {
        this(player, page, player.blockPosition().above());
    }

    public AccessoryPageEvent(ServerPlayer player, int page, BlockPos pos) {
        this.player = player;
        this.page = page;
        this.pos = pos.immutable();
    }

    public ServerPlayer player() {
        return player;
    }

    public int page() {
        return page;
    }

    /** 请求发出前的菜单打开坐标，导航到下一页时按来源再向上移一格。 */
    public BlockPos pos() {
        return pos;
    }
}
