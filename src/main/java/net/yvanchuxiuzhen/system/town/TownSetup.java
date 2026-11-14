package net.yvanchuxiuzhen.system.town;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.town.content.TownBlock;
import net.yvanchuxiuzhen.system.town.content.TownBlockEntities;
import net.yvanchuxiuzhen.system.town.content.TownMenus;
import net.yvanchuxiuzhen.system.town.content.TownPlanningItem;

/**
 * 城镇系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class TownSetup {
    private static final ContentTables CONTENT = new ContentTables("town")
            .blockFactory("town:center", traits -> ContentBlock.create(traits, value -> new TownBlock(value, false)))
            .blockFactory("town:planning", traits -> ContentBlock.create(traits, value -> new TownBlock(value, true)))
            .itemFactory("town:scroll", TownPlanningItem::new);

    private TownSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        TownBlockEntities.register(modBus);
        TownMenus.register(modBus);
        NeoForge.EVENT_BUS.addListener(TownRuntime::commands);
    }
}
