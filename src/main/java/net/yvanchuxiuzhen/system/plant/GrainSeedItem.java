package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.platform.content.ContentItem;

/** 四种采摘食材的种子只在原版耕地上方空位播种并消耗一件。 */
public final class GrainSeedItem extends ContentItem {
    private final GrainCrop crop;

    public GrainSeedItem(Properties properties, List<Component> tooltip, GrainCrop crop) {
        super(properties, tooltip);
        this.crop = crop;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP
                && context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.FARMLAND)) {
            BlockPos plantPos = context.getClickedPos().above();
            if (context.getLevel().isEmptyBlock(plantPos)) {
                context.getLevel().setBlock(plantPos, crop.block(1).defaultBlockState(), 3);
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
