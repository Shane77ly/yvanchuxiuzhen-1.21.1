package net.yvanchuxiuzhen.system.craft.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 待烧陶瓷器只在上表面且上方为空时放置，并保留整份物品实例数据。 */
public final class PotteryBlankItem extends ContentItem {
    public PotteryBlankItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos placePos = context.getClickedPos().above();
        if (!level.isClientSide && context.getClickedFace() == Direction.UP && level.isEmptyBlock(placePos)) {
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taopi"));
            level.setBlock(placePos, block.defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(placePos) instanceof PotteryBlankBlockEntity blank) {
                blank.setItem(0, context.getItemInHand().copyWithCount(1));
                blank.changed();
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
