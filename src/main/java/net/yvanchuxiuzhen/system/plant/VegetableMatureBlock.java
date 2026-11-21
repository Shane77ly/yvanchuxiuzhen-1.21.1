package net.yvanchuxiuzhen.system.plant;

import net.yvanchuxiuzhen.platform.content.BlockTraits;

/** 011–015成熟阶段复用蔬菜族统一的右键采收和阶段回退。 */
public final class VegetableMatureBlock extends VegetableHarvestBlock {
    public VegetableMatureBlock(BlockTraits traits, VegetableCrop crop) {
        super(traits, crop, 4);
    }
}
