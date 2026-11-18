package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.yvanchuxiuzhen.collection.decor.HotOil;
import net.yvanchuxiuzhen.platform.ModId;

/** 热油源与流动状态使用各自的来源动画贴图，不叠加水的群系染色。 */
final class HotOilClient {
    private static final ResourceLocation STILL = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/hot_oil_still");
    private static final ResourceLocation FLOWING = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "block/hot_oil_flow");

    private HotOilClient() {
    }

    static void register(IEventBus modBus) {
        modBus.addListener(HotOilClient::extensions);
        modBus.addListener(HotOilClient::setup);
    }

    private static void extensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING;
            }
        }, HotOil.TYPE.get());
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(HotOil.SOURCE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(HotOil.FLOWING.get(), RenderType.translucent());
        });
    }
}
