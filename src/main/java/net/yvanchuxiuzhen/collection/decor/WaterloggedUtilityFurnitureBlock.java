package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 都承盘和衣架具有含水状态；紫砂壶不实现含水接口。 */
public final class WaterloggedUtilityFurnitureBlock extends UtilityFurnitureBlock implements SimpleWaterloggedBlock {
    public WaterloggedUtilityFurnitureBlock(BlockTraits traits, UtilityFurnitureVariant variant) { super(traits, variant); }
}
