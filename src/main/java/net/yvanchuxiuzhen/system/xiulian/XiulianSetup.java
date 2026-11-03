package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.xiulian.content.BloodEssenceItem;
import net.yvanchuxiuzhen.system.xiulian.content.BloodStoneBlock;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianBlockEntities;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianFluids;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianMenus;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;

/**
 * 修炼系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class XiulianSetup {
    private static final ContentTables CONTENT = new ContentTables("xiulian")
            .blockFactory("xiulian:xixveshi", traits -> ContentBlock.create(traits, BloodStoneBlock::new))
            .itemFactory("xiulian:xvejing", BloodEssenceItem::new);

    private XiulianSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        XiulianFluids.register(modBus);
        XiulianBlockEntities.register(modBus);
        XiulianAttachments.register(modBus);
        XiulianMenus.register(modBus);
        XiulianRules.register();
        modBus.addListener(XiulianSetup::registerPayloads);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> BirthSelection.onLogin((ServerPlayer) event.getEntity()));
        NeoForge.EVENT_BUS.addListener(CultivationTick::onLogin);
        NeoForge.EVENT_BUS.addListener(CultivationTick::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(Delayed::onServerTick);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(XiulianButtonPayload.TYPE, XiulianButtonPayload.STREAM_CODEC, XiulianButtonPayload::handle);
    }
}
