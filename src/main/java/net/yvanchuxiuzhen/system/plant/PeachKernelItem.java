package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 桃核在顶面点击时于上方放置普通桃苗，并按来源顺序扣除一枚。 */
public final class PeachKernelItem extends ContentItem {
    public PeachKernelItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP) {
            Block sapling = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "peachsapling"));
            context.getLevel().setBlock(context.getClickedPos().above(), sapling.defaultBlockState(), Block.UPDATE_ALL);
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
