package net.yvanchuxiuzhen.system.plant;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 柳树垂枝按来源允许手持其他物品原位替换，基础属性与掉落沿用内容表。 */
public final class WillowDroopingBlock extends ContentBlock {
    public WillowDroopingBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().getItem() != asItem();
    }
}
