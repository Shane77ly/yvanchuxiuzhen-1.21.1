package net.yvanchuxiuzhen.system.plant.api;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.plant.SourcePlantSoilBlock;

/** Decor保留土壤注册与资产，Plant提供来源种植规则和只读支持查询。 */
public final class PlantSoilSupport {
    private PlantSoilSupport() {
    }

    public static Block create(BlockTraits traits) {
        return ContentBlock.create(traits, SourcePlantSoilBlock::new);
    }

    public static boolean supports(BlockState soil, BlockState plant) {
        return soil.getBlock() instanceof SourcePlantSoilBlock && SourcePlantSoilBlock.supports(plant);
    }
}
