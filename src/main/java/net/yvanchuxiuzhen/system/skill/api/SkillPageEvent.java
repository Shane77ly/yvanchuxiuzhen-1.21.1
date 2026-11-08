package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 功法与技能装配页请求导航到属性页一或饰品页三，由面板汇总监听，技能系统不依赖导航实现。 */
public final class SkillPageEvent extends Event {
    private final ServerPlayer player;
    private final int page;
    private final BlockPos pos;

    public SkillPageEvent(ServerPlayer player, int page) {
        this(player, page, player.blockPosition().above());
    }

    public SkillPageEvent(ServerPlayer player, int page, BlockPos pos) {
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
