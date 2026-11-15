package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 二踢脚物品：点击顶面空位放置实体，点燃由放置后的实体交互承担。 */
public final class DoubleBangFireworkItem extends ContentItem {
    public DoubleBangFireworkItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos().above();
        if (context.getClickedFace() == Direction.UP && context.getLevel().isEmptyBlock(pos)) {
            if (context.getLevel() instanceof ServerLevel server) {
                DoubleBangFirework firework = FestivalSetup.doubleBangType().get().create(server);
                firework.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0);
                firework.setYBodyRot(0);
                firework.setYHeadRot(0);
                EventHooks.finalizeMobSpawn(firework, server, server.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
                server.addFreshEntity(firework);
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
