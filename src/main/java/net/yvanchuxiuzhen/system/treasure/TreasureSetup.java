package net.yvanchuxiuzhen.system.treasure;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.treasure.content.TreasureBlockEntities;
import net.yvanchuxiuzhen.system.treasure.content.TreasureCasketBlock;
import net.yvanchuxiuzhen.system.treasure.content.TreasureCasketItem;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlock;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockItem;
import net.yvanchuxiuzhen.system.treasure.content.TreasureGroundBlock;
import net.yvanchuxiuzhen.system.treasure.content.TreasureRewardItem;

/**
 * 宝箱系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class TreasureSetup {
    private static final ContentTables CONTENT = new ContentTables("treasure")
            .blockFactory("treasure:casket", traits -> ContentBlock.create(traits, TreasureCasketBlock::new))
            .itemFactory("treasure:casket", TreasureCasketItem::new)
            .itemFactory("treasure:reward", TreasureRewardItem::new)
            .blockFactory("treasure:ground", traits -> ContentBlock.create(traits, TreasureGroundBlock::new))
            .blockFactory("treasure:chest_03", traits -> ContentBlock.create(traits, TreasureChestBlock::new))
            .blockFactory("treasure:chest_11", traits -> ContentBlock.create(traits, TreasureChestBlock.Waterlogged::new))
            .itemFactory("treasure:chest_03", (properties, tooltip) -> new TreasureChestBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.parse("yvanchuxiuzhen:treasure_chest_03")), properties, tooltip))
            .itemFactory("treasure:chest_11", (properties, tooltip) -> new TreasureChestBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.parse("yvanchuxiuzhen:treasure_chest_11")), properties, tooltip));

    private TreasureSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        TreasureBlockEntities.register(modBus);
        TreasureCaskets.register();
        NeoForge.EVENT_BUS.addListener(FinishedEquipmentCommand::register);
        NeoForge.EVENT_BUS.addListener(TreasureCommands::register);
    }
}
