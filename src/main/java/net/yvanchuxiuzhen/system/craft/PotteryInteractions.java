package net.yvanchuxiuzhen.system.craft;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.craft.content.PotteryBlankBlock;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlock;

/** 工艺来源的全局右键入口在潜行持物时仍然执行，不依赖原版方块默认使用判定。 */
public final class PotteryInteractions {
    private PotteryInteractions() {
    }

    public static void use(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
        if (block instanceof PotteryWheelBlock) {
            PotteryRuntime.useWheel(event.getLevel(), event.getPos(), event.getEntity());
        } else if (block instanceof PotteryBlankBlock) {
            PotteryRuntime.useBlank(event.getLevel(), event.getPos(), event.getEntity());
        } else {
            return;
        }
    }
}
