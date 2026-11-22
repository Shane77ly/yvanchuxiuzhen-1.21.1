package net.yvanchuxiuzhen.system.equipment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationSecondEvent;

/**
 * 穿界古镜的落点被方块占用时向上推出（来源 {@code PlayerTick} 中读取体质键 {@code kaqiangpanding} 后调用 {@code Xiangshang} 的分支）：
 * 从玩家当前 Y 起向上最多检查 60 格，遇到第一个空气格后传送，并清除标记。检查与修炼每秒推进同一节拍。
 */
final class Fabao013Unstuck {
    private static final int SEARCH = 60;

    private Fabao013Unstuck() {
    }

    static void onCultivationSecond(CultivationSecondEvent event) {
        ServerPlayer player = event.player();
        if (!Cultivation.tizhiBoolean(player, "kaqiangpanding")) {
            return;
        }
        BlockPos feet = BlockPos.containing(player.getX(), player.getY(), player.getZ());
        if (player.level().isEmptyBlock(feet)) {
            return;
        }
        double y = player.getY();
        for (int index = 0; index < SEARCH; index++) {
            if (player.level().isEmptyBlock(BlockPos.containing(player.getX(), y, player.getZ()))) {
                break;
            }
            y++;
        }
        player.teleportTo(player.getX(), y, player.getZ());
        Cultivation.setTizhiBoolean(player, "kaqiangpanding", false);
    }
}
