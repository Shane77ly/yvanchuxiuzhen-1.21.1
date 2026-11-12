package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 棉种和麻种仅在来源规定的耕地上播种。 */
public final class FiberSeedItem extends ContentItem {
    private final FiberCrop crop;

    public FiberSeedItem(Properties properties, List<Component> tooltip, FiberCrop crop) {
        super(properties, tooltip);
        this.crop = crop;
    }

    public static FiberSeedItem create(Properties properties, List<Component> tooltip, String itemId) {
        return new FiberSeedItem(properties, tooltip, FiberCrop.fromSeedId(itemId));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP
                && context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.FARMLAND)) {
            BlockPos pos = context.getClickedPos().above();
            if (context.getLevel().isEmptyBlock(pos)) {
                BlockState state = crop.block(1).defaultBlockState();
                context.getLevel().setBlock(pos, state, Block.UPDATE_ALL);
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
