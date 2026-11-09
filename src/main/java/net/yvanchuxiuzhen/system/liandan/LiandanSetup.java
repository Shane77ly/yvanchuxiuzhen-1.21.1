package net.yvanchuxiuzhen.system.liandan;

import net.neoforged.bus.api.IEventBus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlock;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockItem;
import net.yvanchuxiuzhen.system.liandan.content.LiandanBlockEntities;
import net.yvanchuxiuzhen.system.liandan.content.LiandanMenus;
import net.yvanchuxiuzhen.system.liandan.content.MedicineContainerBlock;
import net.yvanchuxiuzhen.system.liandan.content.MedicineContainerItem;
import net.yvanchuxiuzhen.system.liandan.content.MedicineSeparationBlock;
import net.yvanchuxiuzhen.system.liandan.content.PharmacologyBlock;
import net.yvanchuxiuzhen.system.liandan.network.AlchemyButtonPayload;
import net.yvanchuxiuzhen.system.liandan.network.MedicineSeparationPayload;
import net.yvanchuxiuzhen.system.liandan.network.PharmacologyPayload;

/**
 * 炼丹系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class LiandanSetup {
    private static final ContentTables CONTENT = new ContentTables("liandan")
            .blockFactory("liandan:alchemy", traits -> ContentBlock.create(traits, AlchemyBlock::new))
            .blockFactory("liandan:medicine_container", MedicineContainerBlock::create)
            .blockFactory("liandan:medicine_separation", traits -> ContentBlock.create(traits, MedicineSeparationBlock::new))
            .blockFactory("liandan:pharmacology", traits -> ContentBlock.create(traits, PharmacologyBlock::new))
            .itemFactory("liandan:medicine_container", (properties, tooltip) -> new MedicineContainerItem(properties, tooltip, true))
            .itemFactory("liandan:medicine_container_empty_box", (properties, tooltip) -> new MedicineContainerItem(properties, tooltip, false));

    private LiandanSetup() {
    }

    public static void register(IEventBus modBus) {
        for (int tier = 1; tier <= 8; tier++) {
            String id = "alchemy_0" + tier;
            CONTENT.itemFactory("liandan:" + id, (properties, tooltip) -> new AlchemyBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id)), properties, tooltip));
        }
        CONTENT.register(modBus);
        LiandanBlockEntities.register(modBus);
        LiandanMenus.register(modBus);
        modBus.addListener(LiandanSetup::payloads);
        NeoForge.EVENT_BUS.addListener(AlchemyRuntime::placed);
        NeoForge.EVENT_BUS.addListener(AlchemyRuntime::rightClick);
        NeoForge.EVENT_BUS.addListener(AlchemyRuntime::heat);
        NeoForge.EVENT_BUS.addListener(AlchemyRuntime::exhausted);
        NeoForge.EVENT_BUS.addListener(MedicineFeeding::onInteract);
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(AlchemyButtonPayload.TYPE, AlchemyButtonPayload.STREAM_CODEC, AlchemyButtonPayload::handle);
        event.registrar("1").playToServer(MedicineSeparationPayload.TYPE, MedicineSeparationPayload.STREAM_CODEC, MedicineSeparationPayload::handle);
        event.registrar("1").playToServer(PharmacologyPayload.TYPE, PharmacologyPayload.STREAM_CODEC, PharmacologyPayload::handle);
    }
}
