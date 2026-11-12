package net.yvanchuxiuzhen.system.craft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockEntity;
import net.yvanchuxiuzhen.system.craft.content.StoneMillMenu;

/** 潜行打开指定设备库存，普通使用立即研磨四轮并请求来源动画。 */
public final class StoneMillRuntime {
    private StoneMillRuntime() {
    }

    public static void use(Level world, BlockPos pos, Player player) {
        if (!(world.getBlockEntity(pos) instanceof StoneMillBlockEntity mill)) return;
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer opener) {
                opener.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new StoneMillMenu(id, inventory, pos),
                        Component.translatable("block.yvanchuxiuzhen." + mill.id())), pos);
            }
        } else {
            if (world instanceof ServerLevel level) Grinding.grind(level, Vec3.atLowerCornerOf(pos), "4");
            mill.animate(1);
        }
    }
}
