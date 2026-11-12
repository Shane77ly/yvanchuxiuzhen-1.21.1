package net.yvanchuxiuzhen.system.plant;

import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 通过来源编号与阶段选择豆科方块类型，内容表继续保存静态物理与外观属性。 */
public final class LegumeBlocks {
    private LegumeBlocks() {
    }

    public static Block create(BlockTraits traits) {
        String blockId = traits.row().id();
        LegumeCrop crop = LegumeCrop.fromBlockId(blockId);
        int stage = crop.stageFromBlockId(blockId);
        if (crop.supportCrop()) {
            if (crop.harvests(stage)) {
                return ContentBlock.create(traits, row -> new LegumeSupportBlock.Harvestable(row, crop, stage));
            }
            return ContentBlock.create(traits, row -> new LegumeSupportBlock.Growing(row, crop, stage));
        }
        if (crop.harvests(stage)) return new LegumeFlowerBlock.Harvestable(traits, crop, stage);
        if (crop.hasBonemeal(stage)) return new LegumeFlowerBlock.Growing(traits, crop, stage);
        return new LegumeFlowerBlock(traits, crop, stage);
    }
}
