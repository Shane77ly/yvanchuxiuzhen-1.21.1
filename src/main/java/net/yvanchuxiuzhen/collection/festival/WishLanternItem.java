package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.event.EventHooks;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 祈愿灯物品：点击顶面且正上方为空气时放出一盏实体。 */
public final class WishLanternItem extends ContentItem {
    public WishLanternItem(Item.Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos().above();
        if (context.getClickedFace() == Direction.UP && context.getLevel().isEmptyBlock(pos)) {
            if (context.getLevel() instanceof ServerLevel server) {
                WishLantern lantern = FestivalSetup.wishLanternType().get().create(server);
                RandomSource random = RandomSource.create();
                lantern.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Mth.nextInt(random, 0, 355), 0);
                lantern.setYBodyRot(Mth.nextInt(random, 0, 355));
                lantern.setYHeadRot(Mth.nextInt(random, 0, 355));
                EventHooks.finalizeMobSpawn(lantern, server, server.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null);
                server.addFreshEntity(lantern);
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
