package net.yvanchuxiuzhen.system.craft.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.CraftBlockEntities;
import net.yvanchuxiuzhen.system.craft.content.CraftMenus;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlockItem;

/** 陶轮菜单、Geo物品及陶坯燃烧附加渲染的客户端装配。 */
public final class PotteryClientSetup {
    private PotteryClientSetup() {
    }

    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CraftBlockEntities.POTTERY_WHEEL.get(), PotteryWheelRenderer::new);
        event.registerBlockEntityRenderer(CraftBlockEntities.POTTERY_BLANK.get(), PotteryBlankRenderer::new);
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(PotteryClientSetup::renderers);
        modBus.addListener(PotteryClientSetup::screens);
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            PotteryWheelBlockItem item = (PotteryWheelBlockItem) BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taolun"));
            item.setRenderProvider(new PotteryWheelItemRenderer.Provider());
        }));
    }

    public static void screens(RegisterMenuScreensEvent event) {
        event.register(CraftMenus.POTTERY_WHEEL.get(), PotteryWheelScreen::new);
    }
}
