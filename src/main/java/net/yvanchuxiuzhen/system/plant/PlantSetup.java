package net.yvanchuxiuzhen.system.plant;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.plant.content.MulberryLeavesBlock;
import net.yvanchuxiuzhen.system.plant.content.MulberrySaplingBlock;

/** 植物系统的启动装配，只在Mod构造时调用一次。 */
public final class PlantSetup {
    private static final ContentTables CONTENT = new ContentTables("plant")
            .blockFactory("plant:shicai001_01", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_001, 1))
            .blockFactory("plant:shicai001_02", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_001, 2))
            .blockFactory("plant:shicai001_03", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_001, 3))
            .blockFactory("plant:shicai001_04", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_001, 4))
            .blockFactory("plant:shicai001_05", traits -> new CerealDoubleStageBlock(traits, CerealCrop.SHICAI_001, 5))
            .blockFactory("plant:shicai001_06", traits -> new CerealDoubleStageBlock(traits, CerealCrop.SHICAI_001, 6))
            .blockFactory("plant:shicai001_07", traits -> new CerealDoubleStageBlock(traits, CerealCrop.SHICAI_001, 7))
            .blockFactory("plant:shicai001_08", traits -> new CerealMatureBlock(traits, CerealCrop.SHICAI_001))
            .itemFactory("plant:shicai001_seed", (properties, tooltip) -> new CerealSeedItem(properties, CerealCrop.SHICAI_001))
            .blockFactory("plant:shicai002_01", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_002, 1))
            .blockFactory("plant:shicai002_02", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_002, 2))
            .blockFactory("plant:shicai002_03", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_002, 3))
            .blockFactory("plant:shicai002_04", traits -> new CerealSingleStageBlock(traits, CerealCrop.SHICAI_002, 4))
            .blockFactory("plant:shicai002_05", traits -> new CerealDoubleStageBlock(traits, CerealCrop.SHICAI_002, 5))
            .blockFactory("plant:shicai002_06", traits -> new CerealDoubleStageBlock(traits, CerealCrop.SHICAI_002, 6))
            .blockFactory("plant:shicai002_07", traits -> new CerealDoubleStageBlock(traits, CerealCrop.SHICAI_002, 7))
            .blockFactory("plant:shicai002_08", traits -> new CerealMatureBlock(traits, CerealCrop.SHICAI_002))
            .itemFactory("plant:shicai002_seed", (properties, tooltip) -> new CerealSeedItem(properties, CerealCrop.SHICAI_002))
            .blockFactory("plant:mulberry_sapling", MulberrySaplingBlock::new)
            .blockFactory("plant:mulberry_leaves", MulberryLeavesBlock::new);

    private PlantSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        PlantFeatures.register(modBus);
        NeoForge.EVENT_BUS.addListener(MulberryGrowth::afterServerTick);
        NeoForge.EVENT_BUS.addListener(MulberryGrowth::stopped);
    }
}
