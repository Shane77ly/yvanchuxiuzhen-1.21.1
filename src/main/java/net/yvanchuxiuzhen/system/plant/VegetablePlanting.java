package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** 蔬菜播种的服务端世界写入口；Cooking可食用大蒜只通过公开操作调用其种植规则。 */
public final class VegetablePlanting {
    private VegetablePlanting() {
    }

    /** 仅处理01502大蒜的原生方块使用入口，食用和食物属性仍由Cooking拥有。 */
    public static InteractionResult plantGarlic(UseOnContext context) {
        return plant(context, VegetableCrop.SHICAI_015);
    }

    static InteractionResult plant(UseOnContext context, VegetableCrop crop) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP
                && context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.FARMLAND)) {
            BlockPos plantPos = context.getClickedPos().above();
            if (context.getLevel().isEmptyBlock(plantPos)) {
                context.getLevel().setBlock(plantPos, crop.block(1).defaultBlockState(), Block.UPDATE_ALL);
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
