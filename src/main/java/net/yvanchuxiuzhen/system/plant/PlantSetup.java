package net.yvanchuxiuzhen.system.plant;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.plant.content.MulberryLeavesBlock;
import net.yvanchuxiuzhen.system.plant.content.MulberrySaplingBlock;

/** 植物系统的启动装配，只在Mod构造时调用一次。 */
public final class PlantSetup {
    private static final ContentTables CONTENT = new ContentTables("plant")
            .blockFactory("plant:shicai001_01", traits -> new Shicai001SingleStageBlock(traits, 1))
            .blockFactory("plant:shicai001_02", traits -> new Shicai001SingleStageBlock(traits, 2))
            .blockFactory("plant:shicai001_03", traits -> new Shicai001SingleStageBlock(traits, 3))
            .blockFactory("plant:shicai001_04", traits -> new Shicai001SingleStageBlock(traits, 4))
            .blockFactory("plant:shicai001_05", traits -> new Shicai001DoubleStageBlock(traits, 5))
            .blockFactory("plant:shicai001_06", traits -> new Shicai001DoubleStageBlock(traits, 6))
            .blockFactory("plant:shicai001_07", traits -> new Shicai001DoubleStageBlock(traits, 7))
            .blockFactory("plant:shicai001_08", Shicai001MatureBlock::new)
            .itemFactory("plant:shicai001_seed", (properties, tooltip) -> new Shicai001SeedItem(properties))
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
