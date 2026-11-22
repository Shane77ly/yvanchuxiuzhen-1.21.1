package net.yvanchuxiuzhen.system.guchong.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 单虫物品只在方块上方为空且点击顶面时释放；状态恢复由共享生命周期装配负责。 */
public final class GuchongReleaseItem extends ContentItem {
    private final String entityId;

    public GuchongReleaseItem(Properties properties, List<Component> tooltip, String entityId) {
        super(properties, tooltip);
        this.entityId = entityId;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) return InteractionResult.SUCCESS;
        BlockPos pos = context.getClickedPos().above();
        if (context.getClickedFace() != Direction.UP || !level.isEmptyBlock(pos)) return InteractionResult.SUCCESS;
        ItemStack stack = context.getItemInHand();
        GuchongLifecycle.releaseSingleItem(level, pos, context.getPlayer(), stack, entityId);
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
