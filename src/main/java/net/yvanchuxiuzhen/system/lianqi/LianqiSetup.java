package net.yvanchuxiuzhen.system.lianqi;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiBlockEntities;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiMenus;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlock;
import net.yvanchuxiuzhen.system.lianqi.content.TripodPlacementItem;
import net.yvanchuxiuzhen.system.lianqi.network.TripodButtonPayload;

/**
 * 炼器系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class LianqiSetup {
    private static final ContentTables CONTENT = new ContentTables("lianqi")
            .blockFactory("lianqi:tripod", traits -> ContentBlock.create(traits, TripodBlock::new))
            .itemFactory("lianqi:tripod_placement", TripodPlacementItem::new);

    private LianqiSetup() {
    }

    public static void register(IEventBus modBus) {
        LianqiRules.register();
        CONTENT.register(modBus);
        LianqiBlockEntities.register(modBus);
        LianqiMenus.register(modBus);
        modBus.addListener(LianqiSetup::payloads);
        NeoForge.EVENT_BUS.addListener(TripodRuntime::heat);
        NeoForge.EVENT_BUS.addListener(TripodRuntime::exhausted);
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TripodButtonPayload.TYPE, TripodButtonPayload.STREAM_CODEC, TripodButtonPayload::handle);
    }
}
