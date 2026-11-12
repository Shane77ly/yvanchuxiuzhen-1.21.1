package net.yvanchuxiuzhen.system.craft;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlock;

/** 来源全局右键在原版潜行和物品使用判定之前打开木匠菜单，随后保留原版交互链。 */
public final class CarpentryInteractions {
    private CarpentryInteractions() {
    }

    public static void use(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != event.getEntity().getUsedItemHand()) return;
        if (event.getLevel().getBlockState(event.getPos()).getBlock() instanceof CarpentryBlock) {
            CarpentryRuntime.use(event.getLevel(), event.getPos(), event.getEntity());
        }
    }
}
