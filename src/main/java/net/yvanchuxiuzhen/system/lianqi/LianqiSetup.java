package net.yvanchuxiuzhen.system.lianqi;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.BlueprintTableBlock;
import net.yvanchuxiuzhen.system.lianqi.content.PoisonCauldronBlock;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableBlock;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableBlockItem;
import net.yvanchuxiuzhen.system.lianqi.content.SewingBasketBlock;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiBlockEntities;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiMenus;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlock;
import net.yvanchuxiuzhen.system.lianqi.content.TripodPlacementItem;
import net.yvanchuxiuzhen.system.lianqi.network.TripodButtonPayload;
import net.yvanchuxiuzhen.system.lianqi.network.BlueprintTableButtonPayload;
import net.yvanchuxiuzhen.system.lianqi.network.RepairTableButtonPayload;
import net.yvanchuxiuzhen.system.lianqi.network.SewingBasketPayload;

/**
 * 炼器系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class LianqiSetup {
    private static final ContentTables CONTENT = new ContentTables("lianqi")
            .blockFactory("lianqi:tripod", traits -> ContentBlock.create(traits, TripodBlock::new))
            .blockFactory("lianqi:repair_table", traits -> ContentBlock.create(traits, RepairTableBlock::new))
            .blockFactory("lianqi:sewing_basket", traits -> ContentBlock.create(traits, SewingBasketBlock::new))
            .blockFactory("lianqi:blueprint_table", traits -> ContentBlock.create(traits, BlueprintTableBlock::new))
            .blockFactory("lianqi:poison_cauldron", traits -> ContentBlock.create(traits, PoisonCauldronBlock::new))
            .itemFactory("lianqi:repair_table", (properties, tooltip) -> new RepairTableBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "xiulitai")), properties, tooltip))
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
        NeoForge.EVENT_BUS.addListener(RepairTableRuntime::rightClick);
        NeoForge.EVENT_BUS.addListener(PoisonCrafting::rightClickBlock);
        NeoForge.EVENT_BUS.addListener(PoisonCrafting::rightClickItem);
        NeoForge.EVENT_BUS.addListener(LianqiDelays::tick);
        NeoForge.EVENT_BUS.addListener(LianqiDelays::stopped);
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TripodButtonPayload.TYPE, TripodButtonPayload.STREAM_CODEC, TripodButtonPayload::handle);
        event.registrar("1").playToServer(RepairTableButtonPayload.TYPE, RepairTableButtonPayload.STREAM_CODEC, RepairTableButtonPayload::handle);
        event.registrar("1").playToServer(SewingBasketPayload.TYPE, SewingBasketPayload.STREAM_CODEC, SewingBasketPayload::handle);
        event.registrar("1").playToServer(BlueprintTableButtonPayload.TYPE, BlueprintTableButtonPayload.STREAM_CODEC, BlueprintTableButtonPayload::handle);
    }
}
