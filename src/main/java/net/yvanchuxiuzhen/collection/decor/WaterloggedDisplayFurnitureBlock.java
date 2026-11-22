package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 供桌与博古架二至五拥有原生含水交互；一号架保持无含水接口。 */
public final class WaterloggedDisplayFurnitureBlock extends DisplayFurnitureBlock implements SimpleWaterloggedBlock {
    public WaterloggedDisplayFurnitureBlock(BlockTraits traits, DisplayFurnitureVariant variant) { super(traits, variant); }
}
