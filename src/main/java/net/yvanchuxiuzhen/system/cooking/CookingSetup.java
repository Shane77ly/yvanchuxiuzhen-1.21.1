package net.yvanchuxiuzhen.system.cooking;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.cooking.content.CookingBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.CookingMenus;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlock;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockItem;
import net.yvanchuxiuzhen.system.cooking.network.CookingButtonPayload;

/**
 * 烹饪系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CookingSetup {
    private static final ContentTables CONTENT = new ContentTables("cooking")
            .blockFactory("cooking:iron_pot", traits -> ContentBlock.create(traits, CookingPotBlock.Iron::new))
            .blockFactory("cooking:clay_pot", traits -> ContentBlock.create(traits, CookingPotBlock.Clay::new))
            .itemFactory("cooking:iron_pot", (properties, tooltip) -> new CookingPotBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fooddaguo")), properties, tooltip))
            .itemFactory("cooking:clay_pot", (properties, tooltip) -> new CookingPotBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fooddunguo")), properties, tooltip));

    private CookingSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.effectBehavior("effect_031", new FastingEffect());
        CONTENT.register(modBus);
        CookingBlockEntities.register(modBus);
        CookingMenus.register(modBus);
        modBus.addListener(CookingSetup::payloads);
        NeoForge.EVENT_BUS.addListener(CookingRuntime::rightClick);
        NeoForge.EVENT_BUS.addListener(CookingDelays::tick);
        NeoForge.EVENT_BUS.addListener(CookingDelays::stopped);
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(CookingButtonPayload.TYPE, CookingButtonPayload.STREAM_CODEC, CookingButtonPayload::handle);
    }
}
