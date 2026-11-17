package net.yvanchuxiuzhen.system.plant;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 尸草根据当前放置上下文允许非自身物品在原格替换。 */
final class CorpseGrassBlock extends ContentFlower {
    CorpseGrassBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().getItem() != asItem();
    }
}
