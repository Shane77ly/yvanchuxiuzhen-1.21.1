package net.yvanchuxiuzhen.smoke;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;

/** 检查用的平地：以玩家脚下为准铺一块石头地面（东侧 14 格、其余各 8 格），上方 7 格清空，使弹道与位移不受随机地形影响。 */
final class Arena {
    private Arena() {
    }

    static void level(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos feet = player.blockPosition();
        for (int dx = -8; dx <= 14; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                level.setBlock(feet.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), 2);
                for (int dy = 0; dy <= 6; dy++) {
                    level.setBlock(feet.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }
}
