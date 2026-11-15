package net.yvanchuxiuzhen.collection.tiandao;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.BlockDisplayLock;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/**
 * 天道内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class TiandaoSetup {
    private static final ContentTables CONTENT = new ContentTables("tiandao")
            .blockFactory("tiandao:multi_structure", traits -> ContentBlock.create(traits, MultiStructureBlock::new))
            .itemFactory("tiandao:movement", MovementToolItem::new)
            .itemFactory("tiandao:display_lock", DisplayLockItem::new);

    private TiandaoSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        MultiStructureContent.register(modBus);
        BlockDisplayLock.register(modBus);
        NeoForge.EVENT_BUS.addListener(DisplayLockItem::interact);
        NeoForge.EVENT_BUS.addListener(FixedBlockProtection::onBreak);
    }
}
