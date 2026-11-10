package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

/** Shicai001种子仅在朝上点击原版耕地空位时播种。 */
public final class Shicai001SeedItem extends Item {
    public Shicai001SeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP
                && context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.FARMLAND)) {
            BlockPos plantPos = context.getClickedPos().above();
            if (context.getLevel().isEmptyBlock(plantPos)) {
                context.getLevel().setBlock(plantPos, Shicai001Behavior.block(1).defaultBlockState(), 3);
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
