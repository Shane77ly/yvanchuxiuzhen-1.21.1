package net.yvanchuxiuzhen.system.xiulian;

import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.xiulian.content.AscensionTokenItem;
import net.yvanchuxiuzhen.system.xiulian.content.BloodEssenceItem;
import net.yvanchuxiuzhen.system.xiulian.content.BloodStoneBlock;
import net.yvanchuxiuzhen.system.xiulian.content.ClanTokenItem;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianBlockEntities;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianFluids;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianMenus;
import net.yvanchuxiuzhen.system.xiulian.data.XiulianAttachments;
import net.yvanchuxiuzhen.system.xiulian.network.XiulianButtonPayload;
import net.yvanchuxiuzhen.system.xiulian.tribulation.Tribulations;

/**
 * 修炼系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class XiulianSetup {
    private static final ContentTables CONTENT = new ContentTables("xiulian")
            .blockFactory("xiulian:xixveshi", traits -> ContentBlock.create(traits, BloodStoneBlock::new))
            .itemFactory("xiulian:xvejing", BloodEssenceItem::new)
            .itemFactory("xiulian:ling_09", AscensionTokenItem::new)
            .itemFactory("xiulian:ling_10", ClanTokenItem::new);

    private XiulianSetup() {
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }

    public static void register(IEventBus modBus) {
        Tribulations.register(CONTENT);
        CONTENT.register(modBus);
        XiulianFluids.register(modBus);
        XiulianBlockEntities.register(modBus);
        XiulianAttachments.register(modBus);
        XiulianMenus.register(modBus);
        XiulianRules.register();
        modBus.addListener(XiulianSetup::registerPayloads);
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> BirthSelection.onLogin((ServerPlayer) event.getEntity()));
        NeoForge.EVENT_BUS.addListener(Rebirth::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(CultivationTick::onLogin);
        NeoForge.EVENT_BUS.addListener(CultivationTick::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(Delayed::onServerTick);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(XiulianButtonPayload.TYPE, XiulianButtonPayload.STREAM_CODEC, XiulianButtonPayload::handle);
    }
}
