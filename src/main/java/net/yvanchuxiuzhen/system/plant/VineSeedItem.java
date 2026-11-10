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

/** 六种种子在泥土或草材质顶面空位播种；来源耕地分支仅扣种，不放苗。 */
public final class VineSeedItem extends ContentItem {
    private final VineCrop crop;

    public VineSeedItem(Properties properties, List<Component> tooltip, VineCrop crop) {
        super(properties, tooltip);
        this.crop = crop;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        super.useOn(context);
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP) {
            BlockPos pos = context.getClickedPos().above();
            if (context.getLevel().isEmptyBlock(pos)) {
                BlockState soil = context.getLevel().getBlockState(context.getClickedPos());
                if (soil.is(Blocks.FARMLAND)) {
                    context.getItemInHand().shrink(1);
                } else if (PlantSoils.dirtOrGrass(soil)) {
                    context.getLevel().setBlock(pos, crop.block(crop.firstStage()).defaultBlockState(), Block.UPDATE_ALL);
                    context.getItemInHand().shrink(1);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
